import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.AvroSchema
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.darkkeks.soa.client.ClientData
import me.darkkeks.soa.client.adGroup
import me.darkkeks.soa.client.banner
import me.darkkeks.soa.client.campaign
import me.darkkeks.soa.client.clientData
import me.darkkeks.soa.client.link
import me.darkkeks.soa.client.multiplier
import me.darkkeks.soa.client.strategy
import org.apache.avro.Schema
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

val formats: List<Format> = listOf(
    JavaFormat(),
    XmlFormat(),
    JsonFormat(),
    AvroFormat(),
    ProtobufFormat(),
    YamlFormat(),
    MessagePackFormat(),
)

interface Format {
    val name: String
    fun serialize(data: ClientsData): ByteArray
    fun deserialize(data: ByteArray): ClientsData
}

class JavaFormat : Format {
    override val name = "java"
    override fun serialize(data: ClientsData): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).writeObject(data)
        return byteArrayOutputStream.toByteArray()
    }

    override fun deserialize(data: ByteArray): ClientsData {
        return ObjectInputStream(ByteArrayInputStream(data)).readObject() as ClientsData
    }
}

class JsonFormat : Format {
    private val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    override val name = "json"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}

class XmlFormat : Format {
    private val mapper = XmlMapper()
        .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false)
        .registerModule(KotlinModule.Builder().build())

    override val name = "xml"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}

class ProtobufFormat : Format {
    override val name = "protobuf"

    override fun serialize(data: ClientsData): ByteArray {
        val protoData = clientData {
            id = data.id
            name = data.name
            campaigns.addAll(
                data.campaigns.map { camp ->
                    campaign {
                        id = camp.id
                        strategy = strategy {
                            type = camp.strategy.type
                            this.data = camp.strategy.data
                        }
                        if (camp.href != null) {
                            href = camp.href
                        }
                    }
                }
            )
            adGroups.addAll(
                data.adGroups.map { adGroup ->
                    adGroup {
                        id = adGroup.id
                        campaignId = adGroup.campaignId

                        regions.addAll(adGroup.regions)
                        keywords.addAll(adGroup.keywords)
                        multipliers.addAll(
                            adGroup.multipliers.map { mult ->
                                multiplier {
                                    condition = mult.condition
                                    multiplier = mult.multiplier
                                }
                            }
                        )
                    }
                }
            )
            banners.addAll(
                data.banners.map { banner ->
                    banner {
                        id = banner.id
                        adGroupId = banner.adGroupId

                        text1 = banner.text1
                        if (banner.text2 != null) {
                            text2 = banner.text2
                        }
                        if (banner.image != null) {
                            image = banner.image
                        }

                        links.addAll(
                            banner.links.map { link ->
                                link {
                                   href = link.href
                                   text = link.text
                                }
                            }
                        )

                        prices.addAll(banner.prices)
                    }
                }
            )
        }
        return protoData.toByteArray()
    }

    override fun deserialize(data: ByteArray): ClientsData {
        val protoData = ClientData.parseFrom(data)
        return ClientsData(
            id = protoData.id,
            name = protoData.name,
            campaigns = protoData.campaignsList.map { campaign ->
                Campaign(
                    id = campaign.id,
                    strategy = Strategy(
                        type = campaign.strategy.type,
                        data = campaign.strategy.data,
                    ),
                    href = campaign.href,
                )
            },
            adGroups = protoData.adGroupsList.map { adGroup ->
                AdGroup(
                    id = adGroup.id,
                    campaignId = adGroup.campaignId,
                    regions = adGroup.regionsList,
                    keywords = adGroup.keywordsList,
                    multipliers = adGroup.multipliersList.map { multiplier ->
                        Multiplier(
                            condition = multiplier.condition,
                            multiplier = multiplier.multiplier,
                        )
                    }
                )
            },
            banners = protoData.bannersList.map { banner ->
                Banner(
                    id = banner.id,
                    adGroupId = banner.adGroupId,

                    text1 = banner.text1,
                    text2 = if (banner.hasText2()) banner.text2 else null,
                    image = if (banner.hasImage()) banner.image else null,

                    links = banner.linksList.map { link ->
                        Link(
                            href = link.href,
                            text = link.text,
                        )
                    },
                    prices = banner.pricesList,
                )
            }
        )
    }
}

class AvroFormat : Format {
    private val schemaDescription: String = """
        {
          "name": "MyClass",
          "type": "record",
          "namespace": "com.acme.avro",
          "fields": [
            {
              "name": "id",
              "type": "long"
            },
            {
              "name": "name",
              "type": "string"
            },
            {
              "name": "campaigns",
              "type": {
                "type": "array",
                "items": {
                  "name": "campaigns_record",
                  "type": "record",
                  "fields": [
                    {
                      "name": "id",
                      "type": "long"
                    },
                    {
                      "name": "strategy",
                      "type": {
                        "name": "strategy",
                        "type": "record",
                        "fields": [
                          {
                            "name": "type",
                            "type": "int"
                          },
                          {
                            "name": "data",
                            "type": "string"
                          }
                        ]
                      }
                    },
                    {
                      "name": "href",
                      "type": "string"
                    }
                  ]
                }
              }
            },
            {
              "name": "adGroups",
              "type": {
                "type": "array",
                "items": {
                  "name": "adGroups_record",
                  "type": "record",
                  "fields": [
                    {
                      "name": "id",
                      "type": "long"
                    },
                    {
                      "name": "campaignId",
                      "type": "long"
                    },
                    {
                      "name": "regions",
                      "type": {
                        "type": "array",
                        "items": "int"
                      }
                    },
                    {
                      "name": "keywords",
                      "type": {
                        "type": "array",
                        "items": "string"
                      }
                    },
                    {
                      "name": "multipliers",
                      "type": {
                        "type": "array",
                        "items": {
                          "name": "multipliers_record",
                          "type": "record",
                          "fields": [
                            {
                              "name": "condition",
                              "type": "string"
                            },
                            {
                              "name": "multiplier",
                              "type": "int"
                            }
                          ]
                        }
                      }
                    }
                  ]
                }
              }
            },
            {
              "name": "banners",
              "type": {
                "type": "array",
                "items": {
                  "name": "banners_record",
                  "type": "record",
                  "fields": [
                    {
                      "name": "id",
                      "type": "long"
                    },
                    {
                      "name": "adGroupId",
                      "type": "long"
                    },
                    {
                      "name": "text1",
                      "type": "string"
                    },
                    {
                      "name": "text2",
                      "type": ["null", "string"]
                    },
                    {
                      "name": "image",
                      "type": [
                        "string",
                        "null"
                      ]
                    },
                    {
                      "name": "links",
                      "type": {
                        "type": "array",
                        "items": {
                          "name": "links_record",
                          "type": "record",
                          "fields": [
                            {
                              "name": "href",
                              "type": "string"
                            },
                            {
                              "name": "text",
                              "type": "string"
                            }
                          ]
                        }
                      }
                    },
                    {
                      "name": "prices",
                      "type": {
                        "type": "array",
                        "items": "double"
                      }
                    }
                  ]
                }
              }
            }
          ]
        }
    """.trimIndent()

    private val schema: AvroSchema = AvroSchema(Schema.Parser().parse(schemaDescription))

    private val mapper: AvroMapper = AvroMapper()
        .apply {
            registerModule(KotlinModule.Builder().build())
        }

    override val name = "avro"
    override fun serialize(data: ClientsData): ByteArray = mapper.writer(schema).writeValueAsBytes(data)
    override fun deserialize(data: ByteArray): ClientsData = mapper.readerFor(ClientsData::class.java)
        .with(schema).readValue(data)
}

class YamlFormat : Format {
    private val mapper = YAMLMapper()
        .registerModule(KotlinModule.Builder().build())

    override val name = "yaml"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}


class MessagePackFormat : Format {
    private val mapper = ObjectMapper(MessagePackFactory())
        .registerModule(KotlinModule.Builder().build())

    override val name = "msgpack"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}
