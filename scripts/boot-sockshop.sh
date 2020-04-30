gcloud container clusters create sock-shop --zone europe-west1-b --machine-type n1-standard-1 --num-nodes 4

kubectl create namespace sock-shop

#istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.tracing.provider=zipkin --set values.pilot.traceSampling=100

istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.pilot.traceSampling=100

kubectl label namespace sock-shop istio-injection=enabled

kubectl apply -f ../sockshop-istio/1.1-sock-shop-install/1-sock-shop-complete-demo-istio.yaml -n sock-shop

kubectl apply -f ../sockshop-istio/1.1-sock-shop-install/2-sockshop-gateway.yaml -n sock-shop

#kubectl apply -f ../sockshop-istio/1-sock-shop-install/3-virtual-services-all.yaml -n sock-shop

sleep 60

#kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=zipkin -o jsonpath='{.items[0].metadata.name}') 9411:9411 &

kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 16686:16686 &

kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=kiali -o jsonpath='{.items[0].metadata.name}') 20001:20001 &

