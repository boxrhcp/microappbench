import argparse
import subprocess
import os
import requests
import decimal
import time
import numpy as np
import matplotlib.pyplot as plt

CURRENT_FOLDER = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PREPARE_SCRIPT = "prepare-openISBT.sh"
RUN_BENCHMARK = "build/libs/benchmarkTool-1.0-SNAPSHOT-all.jar"
QUERY_CPU = 'sum(rate(container_cpu_usage_seconds_total{container_name!="POD",pod_name!=""}[1m])) by (pod_name)'
QUERY_MEM = 'sum(rate(container_memory_usage_bytes{container_name!="POD",container_name!=""}[1m])) by (pod_name)'
QUERY_SENT = 'sum(rate(container_network_transmit_bytes_total[1m])) by (pod_name)'
QUERY_REC = 'sum(rate(container_network_receive_bytes_total[1m]))by (pod_name)'

info_v1 = {'CPU': [], 'MEM': [], 'SENT': [], 'REC': []}
info_v2 = {'CPU': [], 'MEM': [], 'SENT': [], 'REC': []}

fig, axs = plt.subplots(2, 2)


def _get_path(filename):
    return os.path.join(CURRENT_FOLDER, filename)


def load_args():
    parser = argparse.ArgumentParser(description="This script gets stats")
    parser.add_argument("--verbose", help="The verbose level can be 0, 1 or 2", type=int, default=0)

    return parser.parse_args()


def run_file(file_path, verbose=True):
    process = subprocess.Popen(
        file_path,
        cwd=CURRENT_FOLDER,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True
    )

    while True:
        output = process.stdout.readline()
        err_output = process.stderr.readline()
        if verbose:
            print(output.strip())
            print(err_output.strip())

        return_code = process.poll()
        if return_code is not None:
            for output in process.stdout.readlines():
                if verbose:
                    print(output.strip())
            for output in process.stderr.readlines():
                if verbose:
                    print(output.strip())

            if return_code:
                raise Exception(
                    'File "{}" has not finished successfully'.format(
                        file_path[1],
                    )
                )

            break


def query_api(start, end, flag):
    if flag == 'CPU':
        call = "http://localhost:9090/api/v1/query_range?query=" + QUERY_CPU + "&start=" + str(
            int(start)) + "&end=" + str(int(
            end)) + "&step=1s"
    if flag == 'MEM':
        call = "http://localhost:9090/api/v1/query_range?query=" + QUERY_MEM + "&start=" + str(
            int(start)) + "&end=" + str(int(
            end)) + "&step=1s"
    if flag == 'SENT':
        call = "http://localhost:9090/api/v1/query_range?query=" + QUERY_SENT + "&start=" + str(
            int(start)) + "&end=" + str(int(
            end)) + "&step=1s"
    if flag == 'REC':
        call = "http://localhost:9090/api/v1/query_range?query=" + QUERY_REC + "&start=" + str(
            int(start)) + "&end=" + str(int(
            end)) + "&step=1s"

    print(call)
    response = requests.get(call)
    data = response.json()
    v1_array = []
    v2_array = []
    found_v1 = False
    found_v2 = False
    if data["status"] == "success":
        results = data["data"]["result"]
        for result in results:
            if found_v1 & found_v2:
                break
            if 'pod_name' not in result["metric"]:
                continue
            if "orders-v1" in result["metric"]["pod_name"]:
                for value in result["values"]:
                    v1_array.append(decimal.Decimal(value[1]))
                found_v1 = True
            if "orders-v2" in result["metric"]["pod_name"]:
                for value in result["values"]:
                    v2_array.append(decimal.Decimal(value[1]))
                found_v2 = True

    info_v1[flag].append(v1_array)
    info_v2[flag].append(v2_array)


def get_stats(flag):
    averages = []
    maximums = []
    minimums = []
    diffs = []
    data1 = info_v1[flag]
    data2 = info_v2[flag]
    for i in range(len(data1)):
        sum_av = decimal.Decimal('0')
        maximum = decimal.Decimal('0')
        minimum = decimal.Decimal('1000000')
        for j in range(len(data1[i])):
            if flag == 'CPU':
                diff = (data2[i][j] - data1[i][j])
            else:
                if data1[i][j] != decimal.Decimal('0'):
                    diff = decimal.Decimal('1') - (data2[i][j] / data1[i][j])
                else:
                    continue
            # print(str(v2_results[i][j]) + " - " + str(v1_results[i][j]) + " = " + str(diff))
            diffs.append(diff)
            sum_av += diff
            maximum = maximum.max(diff)
            minimum = minimum.min(diff)
        averages.append(sum_av / decimal.Decimal(len(data1[i])))
        maximums.append(maximum)
        minimums.append(minimum)

    data = np.asarray(diffs, dtype=float)
    if flag == 'CPU':
        axs[0, 0].boxplot(data, showfliers=False)
        axs[0, 0].get_xaxis().set_visible(False)
        axs[0, 0].set_title('CPU Usage')
    if flag == 'MEM':
        axs[0, 1].boxplot(data, showfliers=False)
        axs[0, 1].get_xaxis().set_visible(False)
        axs[0, 1].set_title('Memory Usage')
    if flag == 'SENT':
        axs[1, 0].boxplot(data, showfliers=False)
        axs[1, 0].get_xaxis().set_visible(False)
        axs[1, 0].set_title('Sent Bytes')
    if flag == 'REC':
        axs[1, 1].boxplot(data, showfliers=False)
        axs[1, 1].get_xaxis().set_visible(False)
        axs[1, 1].set_title('Received Bytes')
    print("Data for " + flag)
    for i in range(len(averages)):
        print("Benchmark " + str(i) + " average: " + str(averages[i]) + " max: " + str(maximums[i]))


if __name__ == '__main__':
    args = load_args()

    for x in range(20):
        start = time.time()
        run_file(
            ['java', '-jar', _get_path(RUN_BENCHMARK), '-v'])
        end = time.time()
        query_api(start, end, 'CPU')
        query_api(start, end, 'MEM')
        query_api(start, end, 'SENT')
        query_api(start, end, 'REC')
        time.sleep(5)
    get_stats('CPU')
    get_stats('MEM')
    get_stats('SENT')
    get_stats('REC')
    plt.savefig("image.png")
    print("Finished execution")
