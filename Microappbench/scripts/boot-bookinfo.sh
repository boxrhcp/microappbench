gcloud container clusters create bookinfo --zone europe-west1-b --machine-type n1-standard-1 --num-nodes 3

istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.pilot.traceSampling=100

#istioctl manifest apply --set values.tracing.provider=zipkin

kubectl label namespace default istio-injection=enabled

kubectl apply -f ../bookinfo/platform/kube/bookinfo.yaml

kubectl apply -f ../bookinfo/networking/bookinfo-gateway.yaml

sleep 60

kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 16686:16686 &

kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=kiali -o jsonpath='{.items[0].metadata.name}') 20001:20001 &

