package main

import (
	"fmt"
	"github.com/buger/jsonparser"
	"io/ioutil"
	"log"
	"net/http"
	"time"
)

func main() {
	var username string = "admin"
	var passwd string = "admin"
	var start int64 = 1587602321000000
	var end int64 = 1587602421000000
	var isError bool = false
	client := &http.Client{}
	req, err := http.NewRequest("GET", "http://localhost:20001/kiali/api/namespaces/sock-shop/"+
		"services/user/traces", nil)
	req.SetBasicAuth(username, passwd)
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	bodyText, err := ioutil.ReadAll(resp.Body)
	//fmt.Println(jsonparser.GetString(bodyText,"data","[0]", "traceID"))
	data, _, _, err := jsonparser.Get(bodyText, "data")
	jsonparser.ArrayEach(data, func(traces []byte, dataType jsonparser.ValueType, offset int, err error) {
		spans, _, _, err := jsonparser.Get(traces, "spans")
		if err != nil {
			log.Fatal(err)
		}
		//processInfo, _, _, err:=jsonparser.Get(traces,"processes")
		//if err != nil {
		//	log.Fatal(err)
		//}
		traceID, err := jsonparser.GetString(traces, "traceID")
		if err != nil {
			log.Fatal(err)
		}
		jsonparser.ArrayEach(spans, func(traceData []byte, dataType jsonparser.ValueType, offset int, err error) {
			startTime, err := jsonparser.GetInt(traceData, "startTime")
			if err != nil {
				log.Fatal(err)
			}
			if startTime > start || startTime < end {

				tags, _, _, err := jsonparser.Get(traceData, "tags")
				if err != nil {
					log.Fatal(err)
				}
				var method string
				var url string
				var status string
				jsonparser.ArrayEach(tags, func(tagData []byte, dataType jsonparser.ValueType, offset int, err error) {
					key, err := jsonparser.GetString(tagData, "key")
					if err != nil {
						log.Fatal(err)
					}
					if key == "error" {
						error, err := jsonparser.GetBoolean(tagData, "value")
						if err != nil {
							log.Fatal(err)
						} else {
							isError = error
						}
					} else if key == "http.method" || key == "http.url" || key == "http.status_code" {
						switch key {
						case "http.method":
							method, err = jsonparser.GetString(tagData, "value")
							if err != nil {
								log.Fatal(err)
							}
						case "http.url":
							url, err = jsonparser.GetString(tagData, "value")
							if err != nil {
								log.Fatal(err)
							}
						case "http.status_code":
							status, err = jsonparser.GetString(tagData, "value")
							if err != nil {
								log.Fatal(err)
							}
						}
					}
				})
				if isError {

					processID, err := jsonparser.GetString(traceData, "processID")
					if err != nil {
						log.Fatal(err)
					}
					processName, err := jsonparser.GetString(traces, "processes", processID, "serviceName")
					if err != nil {
						log.Fatal(err)
					}

					recTime := time.Unix(0, startTime * int64(time.Microsecond))
					fmt.Println("Trace ID: " + traceID)
					fmt.Print("Start time: ")
					fmt.Println(recTime)
					fmt.Println("Process " + processID + ": " + processName)
					fmt.Println("HTTP URL: " + url)
					fmt.Println("HTTP Method: " + method)
					fmt.Println("HTTP Status: " + status)
					fmt.Println("-------")
				}
			}
		})
		//jsonparser.ArrayEach(processInfo, func(processes []byte, dataType jsonparser.ValueType, offset int, err error) {
		//	fmt.Println(jsonparser.GetString(processes, "serviceName"))
		//})
		if isError {
			fmt.Println("**************")
		}
		isError = false
	})
	//s := string(bodyText)
	//print(result)
}
