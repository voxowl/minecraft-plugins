package main

import (
	"fmt"
)

func AddFile(code string, friendlyName string, ipAddr string) bool {
	db := Connect()

	res := true

	insert, err := db.Query("INSERT INTO files(`code`, `friendlyName`, `ip`) VALUES(?, ?, ?)", code, friendlyName, ipAddr)

	if err != nil {
		fmt.Println(err)
		res = false
	}

	defer insert.Close()

	return res
}

func GetFile(code string) string {
	db := Connect()

	name := "voxels"

	err := db.QueryRow("SELECT `friendlyName` FROM `files` WHERE `code` = ?", code).Scan(&name)

	if err != nil {
		fmt.Println(err)
	}

	defer db.Close()

	return name
}

func IsDefinitive(code string) int {
	db := Connect()

	definitive := 0

	err := db.QueryRow("SELECT `definitive` FROM `files` WHERE `code` = ?", code).Scan(&definitive)

	if err != nil {
		fmt.Println(err)
	}

	defer db.Close()

	return definitive
}

func IPUploadCount(ipAddr string) int {
	db := Connect()

	count := 0

	err := db.QueryRow("SELECT COUNT(`ip`) AS `count` FROM `files` WHERE `ip` = ? AND `date` >= NOW() - INTERVAL 1 DAY", ipAddr).Scan(&count)

	if err != nil {
		fmt.Println(err)
	}

	defer db.Close()

	return count
}
