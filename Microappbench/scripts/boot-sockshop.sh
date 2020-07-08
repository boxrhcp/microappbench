#create cluster
gcloud container clusters create sock-shop --zone europe-west1-b --machine-type n1-standard-1 --num-nodes 4

#Obtain the nodes created in GKE and mark the first one with a label to store the benchmark containers there
NODE_LIST=( $(kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.name) )
for index in ${!NODE_LIST[@]}; do
    if [ $index == 0 ]
    then
        echo ${NODE_LIST[index]}
        kubectl label nodes ${NODE_LIST[index]} benchmark=true
    else
        kubectl label nodes ${NODE_LIST[index]} benchmark=false
    fi
done

kubectl create namespace sock-shop

#deploy istio automatically
istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.pilot.traceSampling=100
#deploy with trace with zipkin instead of jaeger
#istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.tracing.provider=zipkin --set values.pilot.traceSampling=100

#deploy istio with yaml manually
#kubectl create namespace istio-system
#kubectl apply -f istio.yaml

#label namespace
kubectl label namespace sock-shop istio-injection=enabled

#deploy sock-shop
kubectl apply -f ../../sockshop-istio/1.1-sock-shop-install/1-sock-shop-complete-demo-istio.yaml -n sock-shop
kubectl apply -f ../../sockshop-istio/1.1-sock-shop-install/2-sockshop-gateway.yaml -n sock-shop
#kubectl apply -f ../sockshop-istio/1-sock-shop-install/3-virtual-services-all.yaml -n sock-shop
kubectl apply -f ../../sockshop-istio/1.1-sock-shop-install/3-order-mirror.yaml -n sock-shop


sleep 60

# forward to localhost jaeger and kiali
#kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=zipkin -o jsonpath='{.items[0].metadata.name}') 9411:9411 &
kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 16686:16686 &
kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=kiali -o jsonpath='{.items[0].metadata.name}') 20001:20001 &
kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=prometheus -o jsonpath='{.items[0].metadata.name}') 9090:9090 &


#print ingress external IP
kubectl -n istio-system get svc istio-ingressgateway --no-headers -o jsonpath="{.status.loadBalancer.ingress[*].ip}"