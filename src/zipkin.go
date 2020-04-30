package main

import (
	"fmt"
	"github.com/buger/jsonparser"
	"io/ioutil"
	"log"
	"net/http"
)

func zipkin() {
	var username string = "admin"
	var passwd string = "admin"
	var start string = "1587894305000"
	var end string = "1587982413000"
	client := &http.Client{}
	req, err := http.NewRequest("GET", "http://localhost:9411/api/v2/traces", nil)
	q := req.URL.Query()
	q.Add("serviceName", "user.sock-shop")
	q.Add("endTs", end)
	q.Add("lookback", start)
	req.URL.RawQuery = q.Encode()
	req.SetBasicAuth(username, passwd)

	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	bodyText, err := ioutil.ReadAll(resp.Body)
	jsonparser.ArrayEach(bodyText, func(body []byte, dataType jsonparser.ValueType, offset int, err error) {
		jsonparser.ArrayEach(body, func(traces []byte, dataType jsonparser.ValueType, offset int, err error) {
			traceID, err := jsonparser.GetString(traces, "traceId")
			if err != nil {
				log.Fatal(err)
			}
			fmt.Println(traceID)
		})
	})
}
