package mysql

import (
	"fmt"
)

func AddFile(code string, friendlyName string) bool {
	db := connect()

	res := true

	insert, err := db.Query("INSERT INTO files(`code`, `friendlyName`) VALUES(?, ?)", code, friendlyName)

	if err != nil {
		fmt.Println(err)
		res = false
	}

	defer insert.Close()

	return res
}

func GetFile(code string) string {
	db := connect()

	name := "voxels"

	err := db.QueryRow("SELECT `friendlyName` FROM `files` WHERE `code` = ?", code).Scan(&name)

	if err != nil {
		fmt.Println(err)
	}

	defer db.Close()

	return name
}

func IsDefinitive(code string) int {
	db := connect()

	definitive := 0

	err := db.QueryRow("SELECT `definitive` FROM `files` WHERE `code` = ?", code).Scan(&definitive)

	if err != nil {
		fmt.Println(err)
	}

	defer db.Close()

	return definitive
}
