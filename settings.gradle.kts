rootProject.name = "soa"

include("2-serialization")
include("2-serialization:proto-kt")

include("3-voice-chat")

include("4-rpc-mafia:mafia-client")
include("4-rpc-mafia:mafia-server")
include("4-rpc-mafia:proto-kt")

include("6-rest:common")
include("6-rest:rest")
include("6-rest:report")

include("7-graphql:graphql-user-server")
include("7-graphql:graphql-game-server")
include("7-graphql:graphql-client")
