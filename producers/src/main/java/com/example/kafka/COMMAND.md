# 도픽 생성 명령어
`kafka-topics --bootstrap-server localhost:9092 --create --topic <토픽이름>`

# 토픽 삭제 명령어
`kafka-topics --bootstrap-server localhost:9092 --delete --topic <토픽이름>`

# 컨슈머 메시지 확인
`kafka-console-consumer  --bootstrap-server localhost:9092 --topic <토픽이름> --from-beginning`
- `--from-beginning` : 컨슈머가 만들어진 시점이아닌 이전 메세지 모두를 가져옴
