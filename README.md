# Log Metrics Emitter Application
Design observability stack (log/metrics) using ELK ,Prometheus and Grafana.Simple Java Application which emit metrics and logs deployed in minikube which is consumed by observability stack and visualized in grafana for metrics and in kibana for logs

### Set up ###

   Run on local machine (Application Deployment in minikube)

   1. Start minikube `minikube start`
   2. Set minikube docker environment `eval $(minikube docker-env)`
   2. Set minikube context `kubectl config use-context minikube`
   3. Build the image  `docker build -t <image name> .`
   4. Specify the <image name> in deployment file
   4. Apply deployment file and service file from kubernetes folder `kubectl apply -f deployment.yml` and `kubectl apply -f service.yml`
   5. To hit the exposed endpoints start `minikube service spring-boot-prometheus`
  
   Endpoints configured are
   * /v1/counter - will increase the counter by one each time. We have named our metrics as `custom_counter`
   * /v1/exception - will throw a exception
 

### Installation of Elastic search , Kibana and filebeat in K8 ###
Using ECK [operator](https://www.elastic.co/guide/en/cloud-on-k8s/current/k8s-deploy-elasticsearch.html) . Steps to install elastic cluster are as follows
1. Install custom resource definitions and the operator :
     quickstart.yml is present in kubernetes folder

        kubectl create -f https://download.elastic.co/downloads/eck/1.8.0/crds.yaml
        kubectl apply -f https://download.elastic.co/downloads/eck/1.8.0/operator.yaml
        kubectl apply -f quickstart.yml
3. Get the pod status

        kubectl get pods --selector='elasticsearch.k8s.elastic.co/cluster-name=quickstart'
       
4. Get the credentials.

    A default user named elastic is automatically created with the password stored in a Kubernetes secret:
   
        PASSWORD=$(kubectl get secret quickstart-es-elastic-user -o go-template='{{.data.elastic | base64decode}}')
        echo $PASSWORD   
5. From local check the elastic search endpoint.It will ask for user name and password in browser.Give user name as elastic and password that you got from above echo command
       
        kubectl port-forward service/quickstart-es-http 9200
        curl -u "elastic:$PASSWORD" -k "http://localhost:9200"
       
Let's install KIBANA cluster now

1. To get kibana instance :
     apply kibana.yml which is present inside kubernetes folder

        kubectl apply -f kibana.yml     
       
2. Similar to Elasticsearch, you can retrieve details about Kibana instances and the associated Pods:

        kubectl get kibana
        kubectl get pod --selector='kibana.k8s.elastic.co/name=quickstart' 
       
3.  Use kubectl port-forward to access Kibana from your local workstation:
   
         kubectl port-forward service/quickstart-kb-http 5601
      Open https://localhost:5601 in your browser and add the same password you got from step 4 in elastic cluster setup  


Let's install [FileBeat](https://www.elastic.co/guide/en/beats/filebeat/master/running-on-kubernetes.html)

1. Deploy Filebeat as a DaemonSet to ensure there's a running instance on each node of the cluster.
    Download the manifest
   
        curl -L -O https://raw.githubusercontent.com/elastic/beats/master/deploy/kubernetes/filebeat-kubernetes.yaml
     
       
2. Set the same password in filebeat.yml file at line 86 :

        kubectl apply -f filebeat.yml
       
       
### Verify logs are coming in Kibana ### 

Navigate to http://localhost:5602 and go to [index pattern](https://localhost:5601/app/management/kibana/indexPatterns) and add filebeat* as a new index pattern
After clicking navigate to Discover and filter the logs by adding

      kubernetes.pod.name : "<pod name for which you want to see logs>"
      eg:
      kubernetes.pod.name : "spring-boot-prometheus-9d957847f-sbns5" 
     
    
<img width="1791" alt="logs" src="https://user-images.githubusercontent.com/56393679/135747935-998e5463-071e-4645-9828-bc88d94fec60.png">

### Installation of prometheus and grafana in K8 for metrics ###
1. Prometheus using helm .Below are the steps for the setup

         1. brew install helm (package manager for kubernetes)
         2. helm repo add prometheus-community https://prometheus-community.github.io/helm-charts ( to added "prometheus-community" to your repositories)
         3. helm install prometheus prometheus-community/prometheus
         4. kubectl expose service prometheus-server --type=NodePort --target-port=9090 --name=prometheus-server-np
         5. minikube service prometheus-server-np
  
   `custom_counter_total` in prometheus UI
  ![SAVE_20211003_145154](https://user-images.githubusercontent.com/56393679/135747915-01c9bf20-2239-4208-9de3-edf83b6d4eab.jpg)
  ![SAVE_20211003_145144](https://user-images.githubusercontent.com/56393679/135747898-36071733-deda-44d6-a55d-1ce0471f9298.jpg)

2. Grafana using helm.Below are the steps for the setup

         1. helm repo add grafana https://grafana.github.io/helm-charts
         2. helm install my-release grafana/grafana
         3. kubectl get secret --namespace default my-release-grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
         4. kubectl expose service my-release-grafana --type=NodePort --target-port=3000 --name=grafana-np
         5. minikube service my-release-grafana
   
3. In grafana select datasourse as `prometheus` and configure the panels.   
    K8 metrics
<img width="1719" alt="k8-metrics" src="https://user-images.githubusercontent.com/56393679/135747950-9c66a250-8d0e-4bd4-8647-c4ea365b6fd1.png">
   Application metrics
<img width="1713" alt="dashboard" src="https://user-images.githubusercontent.com/56393679/135747963-3ac4956b-cd0d-4ff9-8668-7868162d7fc6.png">

