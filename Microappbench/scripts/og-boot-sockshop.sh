gcloud container clusters create sock-shop --zone europe-west1-b --machine-type n1-standard-1 --num-nodes 4

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

#istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.tracing.provider=zipkin --set values.pilot.traceSampling=100

istioctl manifest apply --set profile=demo --set values.tracing.enabled=true --set values.pilot.traceSampling=100

#kubectl create namespace istio-system

#kubectl apply -f istio.yaml

kubectl label namespace sock-shop istio-injection=enabled

#kubectl apply -f ../../sockshop-istio/1-sock-shop-install/1-sock-shop-complete-demo-istio.yaml -n sock-shop

kubectl apply -f ../../sockshop-istio/1-sock-shop-install/sock-shop-complete-demo.yaml -n sock-shop

kubectl apply -f ../../sockshop-istio/1-sock-shop-install/2-sockshop-gateway.yaml -n sock-shop

kubectl apply -f ../../sockshop-istio/1-sock-shop-install/3-virtual-services-all.yaml -n sock-shop

sleep 60

#kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=zipkin -o jsonpath='{.items[0].metadata.name}') 9411:9411 &

kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=jaeger -o jsonpath='{.items[0].metadata.name}') 16686:16686 &
kubectl port-forward -n istio-system $(kubectl get pod -n istio-system -l app=kiali -o jsonpath='{.items[0].metadata.name}') 20001:20001 &

kubectl -n istio-system get svc istio-ingressgateway --no-headers -o jsonpath="{.status.loadBalancer.ingress[*].ip}"
