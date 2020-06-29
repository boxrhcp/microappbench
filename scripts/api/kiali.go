package main

import (
	"fmt"
	"github.com/buger/jsonparser"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"
)

func main() {
	var username string = "admin"
	var passwd string = "admin"
	var start int64 = 1592048689000000
	var end int64 = 1592135089000000
	var isError bool = false
	client := &http.Client{}
	req, err := http.NewRequest("GET", "http://localhost:20001/kiali/api/namespaces/sock-shop/"+
		"services/orders/traces", nil)
	req.SetBasicAuth(username, passwd)
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}
	bodyText, err := ioutil.ReadAll(resp.Body)
	f, err := os.Create("api.json")
	if err != nil {
		fmt.Println(err)
		return
	}
	l, err := f.Write(bodyText)
	if err != nil {
		fmt.Println(err)
		f.Close()
		return
	}
	fmt.Println(l, "bytes written successfully")
	err = f.Close()
	if err != nil {
		fmt.Println(err)
		return
	}
	//fmt.Println(jsonparser.GetString(bodyText,"data"))
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

			if startTime > start && startTime < end {
				spanID, err := jsonparser.GetString(traceData, "spanID")
				if err != nil {
					log.Fatal(err)
				}

				duration, err := jsonparser.GetInt(traceData, "duration")
				if err != nil {
					log.Fatal(err)
				}

				tags, _, _, err := jsonparser.Get(traceData, "tags")
				if err != nil {
					log.Fatal(err)
				}
				var method string
				var url string
				var status int
				var respSize string
				var reqSize string
				jsonparser.ArrayEach(tags, func(tagData []byte, dataType jsonparser.ValueType, offset int, err error) {
					key, err := jsonparser.GetString(tagData, "key")
					if err != nil {
						log.Fatal(err)
					}
					switch key {
					case "http.status_code":
						statusNum, err := jsonparser.GetString(tagData, "value")
						if err != nil {
							log.Fatal(err)
						}else{
							status, err = strconv.Atoi(statusNum)
							if err != nil {
								log.Fatal(err)
							}
						}
						if status != 200 && status != 201{
							isError = true
							fmt.Println("ERROR CALL FOUND")
						}
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
					case "response_size":
						respSize, err = jsonparser.GetString(tagData, "value")
						if err != nil {
							log.Fatal(err)
						}
					case "request_size":
						reqSize, err = jsonparser.GetString(tagData, "value")
						if err != nil {
							log.Fatal(err)
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

					recTime := time.Unix(0, startTime*int64(time.Microsecond))
					fmt.Println("Span ID: " + spanID)
					fmt.Print("Start time: ")
					fmt.Println(recTime)
					fmt.Print("Span duration: ")
					fmt.Println(duration)
					fmt.Println("Process " + processID + ": " + processName)
					fmt.Println("HTTP URL: " + url)
					fmt.Println("HTTP Method: " + method)
					fmt.Print("HTTP Status: ")
					fmt.Println(status)
					fmt.Println("Request Size: " + reqSize)
					fmt.Println("Response Size: " + respSize)
					fmt.Println("-------")
				}
			}
		})
		//jsonparser.ArrayEach(processInfo, func(processes []byte, dataType jsonparser.ValueType, offset int, err error) {
		//	fmt.Println(jsonparser.GetString(processes, "serviceName"))
		//})
		if isError {
			fmt.Println("Trace ID: " + traceID)
			fmt.Println("**************")
		}
		isError = false
	})
	//s := string(bodyText)
	//print(result)
}
