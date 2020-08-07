import argparse
import os
import pandas as pd
import cufflinks as cf
import plotly.offline as py
import plotly.graph_objs as go

cf.go_offline()  # required to use plotly offline (no account required).
base = os.path.dirname(os.path.abspath(__file__))
RESULTS_DIR = base + '/templates/results/files/'
FLAGS = ['execTime', 'requestSize', 'responseSize', 'cpu', 'memory', 'sentBytes', 'receivedBytes', 'callError',
         'callMismatch', 'childMismatch']


def load_args():
    parser = argparse.ArgumentParser(description="This script gets stats")
    parser.add_argument("--verbose", help="The verbose level can be 0, 1 or 2", type=int, default=0)
    parser.add_argument("--file", help="file to analyze", type=str)

    return parser.parse_args()


def stats(patternValue):
    rows = []
    for trace in patternValue['issueTraces']:
        for issue in trace['issues']:
            execTime = False
            requestSize = False
            cpu = False
            memory = False
            sentBytes = False
            receivedBytes = False
            responseSize = False
            callError = False
            callMismatch = False
            childMismatch = False
            if "execTime" in issue['issueFlags']:
                execTime = True
            if "requestSize" in issue['issueFlags']:
                requestSize = True
            if "responseSize" in issue['issueFlags']:
                responseSize = True
            if "cpu" in issue['issueFlags']:
                cpu = True
            if "memory" in issue['issueFlags']:
                memory = True
            if "sentBytes" in issue['issueFlags']:
                sentBytes = True
            if "receivedBytes" in issue['issueFlags']:
                receivedBytes = True
            if "callError" in issue['issueFlags']:
                callError = True
            if "callMismatch" in issue['issueFlags']:
                callMismatch = True
            if "childMismatch" in issue['issueFlags']:
                childMismatch = True

            rows.append(
                (trace['operation'], trace['path'], trace['method'], issue['firstVersion']['caller']['name'],
                 issue['firstVersion']['callee']['name'],
                 issue['firstVersion']['method'],
                 issue['issueTag'], execTime, requestSize,
                 responseSize, cpu, memory, sentBytes,
                 receivedBytes, callError, callMismatch, childMismatch))

    df = pd.DataFrame(rows, columns=('operation', 'path', 'method', 'caller', 'callee',
                                     'spanMethod', 'issueTag', 'execTime', 'requestSize', 'responseSize', 'cpu',
                                     'memory',
                                     'sentBytes', 'receivedBytes',
                                     'callError', 'callMismatch', 'childMismatch'))
    for flag in FLAGS:
        grouped = df.groupby(['operation', 'path', 'method', 'caller', 'callee', 'spanMethod'])[
            flag].sum().reset_index().sort_values(flag, ascending=True)
        index = grouped.index.values
        labels = []
        for i in index:
            row = grouped.loc[i, ['operation', 'path', 'method', 'caller', 'callee', 'spanMethod']]
            path_name = row['path']  # row['path'].split("http://")[1].split("/")[1]
            labels.append(
                row['operation'] + ':' + path_name + ':' + row['method'] + ':' + row['caller'] + ':' + row[
                    'callee'] + ':' +
                row['spanMethod'])
        execTime = grouped[flag].values

        fig = go.Figure([
            go.Bar(
                y=labels,
                x=execTime,
                orientation='h'
            )])
        fig.update_layout(
            title=flag + " Count",
            xaxis_title=flag + " flag count",
            yaxis_title="operation:path:method:caller:callee:spanMethod"
        )
        py.plot(fig, filename=RESULTS_DIR + patternValue['name'] + '-' + patternValue['resource']
                .replace("/", "") + '-' + flag + '.html',
                auto_open=False)


if __name__ == '__main__':
    args = load_args()
    data = pd.read_json(args.file)

    for pattern in data['issuePatterns']:
        stats(pattern)
