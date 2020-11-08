package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"time"
)

func uploadFile(w http.ResponseWriter, r *http.Request) {
	fmt.Println("File Upload Endpoint Hit")
	file, handler, err := r.FormFile("myFile")
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
		fmt.Fprintf(w, "E1")
		return
	}
	fmt.Printf("MIME Header: %+v\n", handler.Header)

	c := fmt.Sprintf("%X", time.Now().UnixNano())
	fmt.Println(c + ".vox")
	fileBytes, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Println(err)
	}

	bs := []byte("VOX –")

	for i := 0; i < 5; i++ {
		if bs[i] != fileBytes[i] {
			fmt.Println("Bad vox file")
			fmt.Fprintf(w, "E2")
			return
		}
	}

	f, err := os.Create("voxs\\" + c + ".vox")
	if err != nil {
		fmt.Println(err)
	}

	f.Write(fileBytes)

	fmt.Fprintf(w, "Success\n")
}

func download() {

}

func setupRoutes() {
	http.HandleFunc("/upload", uploadFile)
	http.ListenAndServe(":8080", nil)
}

func main() {
	fmt.Println("hello World")
	setupRoutes()
}
