# docker build --progress=plain --no-cache -t mbt .
docker build --progress=plain -t mbt .

# docker login -u testoptimal
# docker image tag mbt testoptimal/mbt:latest
# docker image push testoptimal/mbt:latest
# docker run -p 8888:8888 testoptimal/mbt:latest