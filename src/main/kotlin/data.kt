import java.io.Serializable

data class ClientsData(
    val id: Long,
    val name: String,
    val campaigns: List<Campaign>,
    val adGroups: List<AdGroup>,
    val banners: List<Banner>,
) : Serializable

data class Campaign(
    val id: Long,
    val strategy: Strategy,
    val href: String?,
) : Serializable

data class Strategy(
    val type: Int,
    val data: String,
) : Serializable

data class AdGroup(
    val id: Long,
    val campaignId: Long,

    val regions: List<Long>,
    val keywords: List<String>,
    val multipliers: List<Multiplier>,
) : Serializable

data class Multiplier(
    val condition: String,
    val multiplier: Int,
) : Serializable

data class Banner(
    val id: Long,
    val adGroupId: Long,

    val text1: String,
    val text2: String?,
    val image: String?,

    val links: List<Link>,
    val prices: List<Double?>,
) : Serializable

data class Link(
    val href: String,
    val text: String,
) : Serializable
