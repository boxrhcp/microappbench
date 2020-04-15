package main

import (
	"io/ioutil"
	"log"
	"net/http"
	"github.com/buger/jsonparser"
)

func main() {
	var username string = "admin"
	var passwd string = "admin"
	client := &http.Client{}
	req, err := http.NewRequest("GET", "http://localhost:20001/kiali/api/namespaces/sock-shop/"+
		"services/user/traces", nil)
	req.SetBasicAuth(username, passwd)
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}

	bodyText, err := ioutil.ReadAll(resp.Body)
	jsonparser.Get(bodyText, "traceID")
	s := string(bodyText)
	print(s)
}
