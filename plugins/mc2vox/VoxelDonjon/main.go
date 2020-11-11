package main

import (
	"fmt"
	"io/ioutil"
	"minecraft-plugins/plugins/mc2vox/VoxelDonjon/mysql"
	"net/http"
	"os"
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
	if handler.Size > 8388608 {
		fmt.Println("File too big")
		fmt.Fprintf(w, "@E1")
		return
	}
	fmt.Printf("MIME Header: %+v\n", handler.Header)

	c := fmt.Sprintf("%X", time.Now().UnixNano())
	fmt.Println(c + ".vox")
	fileBytes, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Println(err)
	}

	bs := []byte("VOX ")

	for i := 0; i < 4; i++ {
		if bs[i] != fileBytes[i] {
			fmt.Println("Bad vox file")
			fmt.Fprintf(w, "@E2")
			return
		}
	}

	mysql.AddFile(c, r.URL.Path[8:])

	f, err := os.Create("voxs\\" + c + ".vox")
	if err != nil {
		fmt.Println(err)
	}

	f.Write(fileBytes)

	fmt.Fprintf(w, "Success\n"+c)
}

func download(w http.ResponseWriter, r *http.Request) {
	if _, err := os.Stat("voxs\\" + r.URL.Path[4:] + ".vox"); os.IsNotExist(err) {
		fmt.Fprintf(w, "File not found or expired")
		fmt.Println("File " + r.URL.Path[4:] + ".vox" + " not found")
		return
	} else if err != nil {
		fmt.Fprintf(w, "Unknown error")
		fmt.Println(err)
		return
	}

	w.Header().Set("Content-Disposition", "attachment; filename="+mysql.GetFile(r.URL.Path[4:])+".vox")
	w.Header().Set("Content-Type", "application/octet-stream")
	http.ServeFile(w, r, "voxs\\"+r.URL.Path[4:]+".vox")
}

func setupRoutes() {
	http.HandleFunc("/upload/", uploadFile)
	http.HandleFunc("/dl/", download)
	http.ListenAndServe(":8080", nil)
}

func main() {
	fmt.Println("hello World")
	setupRoutes()
}
