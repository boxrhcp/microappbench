package main

import (
	"fmt"
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
		"services/front-end-v1/traces", nil)
	req.SetBasicAuth(username, passwd)
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}

	bodyText, err := ioutil.ReadAll(resp.Body)
	//fmt.Println(jsonparser.GetString(bodyText,"data","[0]", "traceID"))
	val, _, _, err:=jsonparser.Get(bodyText,"data")
	jsonparser.ArrayEach(val, func(value []byte, dataType jsonparser.ValueType, offset int, err error) {
		fmt.Println(jsonparser.GetString(value, "traceID"))
	})
	//s := string(bodyText)
	//print(result)
}
