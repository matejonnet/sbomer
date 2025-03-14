# Development

The fastest way to start with SBOMer development is to set up the [development environment](environments.md).

### JDK 17

This project is developed using JDK 17. Not that any features require it, but why not?

You can use https://sdkman.io/ to install and manage JDKs:

```
sdk install java 17.0.6-tem
```

When you enter the project directory you can run:

```
sdk env
```

And you're all set!

> You can add `$HOME/.sdkman/etc/config` following entry:
>
> ```
> sdkman_auto_env=true
> ```
>
> And have the JDK set automatically when entering the directory.

## Preparing Minikube

```
./hack/minikube-setup.sh
```

## Preparing required secret

We need two create a pull secret to be able to pull images from Red Hat registry: `sbomer-redhatio-pull-secret`

### Image registry pull secret

We use image(s) from Red Hat Container Registry. You can authenticate with the registry.redhat.io registry by
[generating a token on this page](https://access.redhat.com/terms-based-registry/#/).

Once you generate the token, download the secret. Ensure that the name of the resource is `sbomer-redhatio-pull-secret`.
It should look similar to this:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: sbomer-redhatio-pull-secret
  labels:
    app.kubernetes.io/name: "sbomer"
data:
  .dockerconfigjson: AWESOMEBASE64CONTENT===
type: kubernetes.io/dockerconfigjson
```

Then apply it.

## Deploying development overlay

```
kubectl apply -k k8s/overlays/development/
```

Running above command for the first time may result in errors. Try again in a few seconds.
Tekton needs to have time to boot.

## Exposing the database

The database is run in the Kubernetes cluster, but our service is outside of it.
To make the service work we need to expose the database.

```
./hack/minikube-expose-db.sh
```

## Exposing the cache

Similarly to the database we need to expose the cache, so that our local builds can perform faster (and we test the usage of th cache at the same time!).

```
./hack/minikube-expose-cache.sh
```

## Running service in development mode

To run the service you just need to run Quarkus in the development mode:

```
./hack/run-dev.sh
```

Please note that the `-Dquarkus.http.host=0.0.0.0` is required, because we need to expose the REST API port
so that the Tekton Task can reach it from inside of a Kubernetes cluster.

If you are using Minikube with KVM driver, you may want to open the 8080/tcp port:

```
sudo firewall-cmd --zone=libvirt --add-port=8080/tcp --permanent --reload
```

### Accessing the SBOMer service

You can forward the port to your local machine to access SBOMer service:

```
kubectl port-forward services/sbomer 8080:80
```

This makes it available at: http://localhost:8080.


## Tests

```
./hack/run-maven.sh clean verify
```

## Building images

All images used by this service can be found in the `src/main/images` directory.

> :warning: Context path
>
> Please note that in order to build images, the context needs to be set to the **root of the repository**.

There are available scripts that help with building all images:

- `hack/build-images-podman.sh`

  Uses local Podman to build all images.

- `hack/build-images-docker.sh`

  Uses local Docker to build all images.

- `hack/build-images-minikube.sh`

  Build images inside of the Minikube environment. This is very useful in case of the `local` development environment
  where you want to test images in a full Kubernetes deployment.

> :arrow_right: Service needs to be built first!
>
> If you don't use above scripts -- please make sure you build the project before building images to ensure the latest
> state of the service is included in the image.

## Starting from scratch

You can always start from scratch, just run these commands:

```
./hack/minikube-delete.sh
./hack/minikube-setup.sh
```
