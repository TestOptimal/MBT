# docker build --progress=plain --no-cache -t testoptimal/base -f DockerfileBase .
docker build --progress=plain -t testoptimal/base -f DockerfileBase .

# docker login -u testoptimal
# docker image tag testoptimal/mbt base:latest
# docker image push testoptimal/base:latest
# docker run -p 8888:8888 testoptimal/base:latest
# docker system prune