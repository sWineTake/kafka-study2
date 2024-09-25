# 도픽 생성 명령어
`kafka-topics --bootstrap-server localhost:9092 --create --topic <토픽이름>`
- `--partitions <파티션갯수>` : 파티션 옵션 추가


# 도픽 상세 정보 조회
`kafka-topics --bootstrap-server localhost:9092 --describe --topic <토픽이름>`

# 토픽 삭제 명령어
`kafka-topics --bootstrap-server localhost:9092 --delete --topic <토픽이름>`

# 토픽 리스트 조회
`kafka-topics --bootstrap-server localhost:9092 --list`

# 컨슈머 메시지 확인
`kafka-console-consumer  --bootstrap-server localhost:9092 --topic <토픽이름> --from-beginning`
- `--from-beginning` : 컨슈머가 만들어진 시점이아닌 이전 메세지 모두를 가져옴
- `--group <그룹이름>` : 해당 그룹으로 연결 (`--topic` 앞에 적어야함)
- `--property print.key=true --property print.value=true` : String 키를 가진 컨슈머로 연결
- `--property print.key=true --property print.value=true --key-deserializer "org.apache.kafka.common.serialization.IntegerDeserializer"` : Integer 키를 가진 컨슈머로 연결
