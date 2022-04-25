package me.darkkeks.soa.serialization.formats

import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.AvroSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.darkkeks.soa.serialization.ClientsData
import org.apache.avro.Schema

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
