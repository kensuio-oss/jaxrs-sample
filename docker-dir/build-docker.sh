#docker run 71dddc7a1631


docker build --no-cache .
docker image ls

docker tag bd2d11a00d7b vidma/wildfly-ksu
#docker tag 123456789 pavel/pavel-build

# docker build --no-cache . |  grep "Successfully built" | sed 's/Successfully built //g' | xargs -I{} docker run {}

#  => => writing image sha256:71dddc7a163150e7016bdecc834fbf073fb4028639b95a83414ce41cf16912d9                                                                    0.0s

#p.s. this dont work easily: docker build - < Dockerfile

docker tag bd2d11a00d7b vidmasze/ksu-wildfly:ksu-wildfly-setup
docker push vidmasze/ksu-wildfly:ksu-wildfly-setup

# ksu-wildfly-setup: digest: sha256:2077404858b2f044946884e3ebfe94cd7ea8d799a4bc127c41ad256395b0de3a size: 1789
