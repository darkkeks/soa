package me.darkkeks.soa.serialization.formats

import me.darkkeks.soa.serialization.AdGroup
import me.darkkeks.soa.serialization.Banner
import me.darkkeks.soa.serialization.Campaign
import me.darkkeks.soa.serialization.ClientsData
import me.darkkeks.soa.serialization.Link
import me.darkkeks.soa.serialization.Multiplier
import me.darkkeks.soa.serialization.Strategy
import me.darkkeks.soa.serialization.proto.ClientData
import me.darkkeks.soa.serialization.proto.adGroup
import me.darkkeks.soa.serialization.proto.banner
import me.darkkeks.soa.serialization.proto.campaign
import me.darkkeks.soa.serialization.proto.clientData
import me.darkkeks.soa.serialization.proto.link
import me.darkkeks.soa.serialization.proto.multiplier
import me.darkkeks.soa.serialization.proto.strategy

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
