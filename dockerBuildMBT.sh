# docker build --progress=plain --no-cache -t testoptimal/mbt -f DockerfileMBT .
docker build --progress=plain -t testoptimal/mbt -f DockerfileMBT .

# docker login -u testoptimal
# docker image tag mbt testoptimal/mbt:latest
# docker image push testoptimal/mbt:latest
# docker run -p 8888:8888 testoptimal/mbt:latest
# docker system prune