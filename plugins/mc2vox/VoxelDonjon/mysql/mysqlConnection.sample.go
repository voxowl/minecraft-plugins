package mysql

import (
	"fmt"
	"database/sql"
	_ "github.com/go-sql-driver/mysql"
)

const (
	dbname = "dbname"
	dbuser = "dbuser"
	dbpassword = "dbpassword"
	dbhost = "dbhostname"
	dbport = "3306"
)

func connect() *sql.DB {
	fmt.Println("MySQL Connection - connecting")

	db, err := sql.Open("mysql", fmt.Sprintf("%s:%s@tcp(%s:%s)/%s", dbuser, dbpassword, dbhost, dbport, dbname))

	if err != nil {
		fmt.Println(err)
		return nil
	} else {
		fmt.Println("MySQL Connection - connected")
	}

	//defer db.Close()

	return db
}