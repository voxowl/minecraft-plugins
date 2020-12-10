package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"minecraft-plugins/plugins/mc2vox/VoxelDonjon/mysql"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"
)

func uploadFile(w http.ResponseWriter, r *http.Request) {
	fmt.Println("File Upload Endpoint Hit")
	file, handler, err := r.FormFile("file")
	if err != nil {
		fmt.Println("Error Retrieving the File")
		fmt.Println(err)
		return
	}
	defer file.Close()
	fmt.Printf("Uploaded File: %+v\n", handler.Filename)
	fmt.Printf("File Size: %+v\n", handler.Size)
	if handler.Size > 8388608 { // If file size is greater than 8MiB
		fmt.Println("File too big")
		fmt.Fprintf(w, "@E1") // Show custom error code
		return
	}
	fmt.Printf("MIME Header: %+v\n", handler.Header)

	c := fmt.Sprintf("%X", time.Now().UnixNano()) // Convert TimeStampNano to Hex String
	fmt.Println(c + ".vox")
	fileBytes, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Println(err)
	}

	bs := []byte("VOX ")

	for i := 0; i < 4; i++ { // Check .vox file header
		if bs[i] != fileBytes[i] {
			fmt.Println("Bad vox file")
			fmt.Fprintf(w, "@E2") // Show custom error code
			return
		}
	}

	mysql.AddFile(c, r.URL.Path[8:])

	f, err := os.Create("voxs/" + c + ".vox")
	if err != nil {
		fmt.Println(err)
	}

	f.Write(fileBytes)

	fmt.Fprintf(w, "Success\n"+c)
}

func download(w http.ResponseWriter, r *http.Request) {
	if _, err := os.Stat("voxs/" + r.URL.Path[4:] + ".vox"); os.IsNotExist(err) {
		fmt.Fprintf(w, "File not found or expired") // Show error to user's web browser
		fmt.Println("File " + r.URL.Path[4:] + ".vox" + " not found")
		return
	} else if err != nil {
		fmt.Fprintf(w, "Unknown error")
		fmt.Println(err)
		return
	}

	w.Header().Set("Content-Disposition", "attachment; filename="+mysql.GetFile(r.URL.Path[4:])+".vox")
	w.Header().Set("Content-Type", "application/octet-stream")
	http.ServeFile(w, r, "voxs/"+r.URL.Path[4:]+".vox")
}

func setupRoutes() {
	http.HandleFunc("/upload/", uploadFile)
	http.HandleFunc("/dl/", download)
	http.ListenAndServe(":80", nil)
}

func autoRM() {
	files, err := ioutil.ReadDir("plugins/mc2vox/VoxelDonjon/voxs")
	if err != nil {
		log.Fatal(err)
	}

	for _, f := range files {
		code := strings.ReplaceAll(f.Name(), ".vox", "")
		if mysql.IsDefinitive(code) == 0 { // If file is persistent
			ts, _ := strconv.ParseInt(code, 16, 64)
			if ts < (time.Now().UnixNano() - 8.64e+13) { // If file date is older than 1 day
				fmt.Println(f.Name() + " expired")
				os.Remove("plugins/mc2vox/VoxelDonjon/voxs/" + f.Name()) // Delete file
			}
		}
	}
}

func repeatTask() {
	ticker := time.NewTicker(10 * time.Minute)
	quit := make(chan struct{})
	go func() {
		for {
			select {
			case <-ticker.C:
				autoRM()
			case <-quit:
				ticker.Stop()
				return
			}
		}
	}()
}

func main() {
	fmt.Println("hello World") // Because ... Hello World
	autoRM()                   // Remove all expired files
	repeatTask()               // Start automatic task
	setupRoutes()              // Setup HTTPServer
}
