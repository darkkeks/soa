package me.darkkeks.soa.serialization

import org.apache.commons.lang3.RandomStringUtils
import java.util.concurrent.ThreadLocalRandom

object DataGeneration {
    fun generateClientData(): ClientsData {
        val campaignCount = randomInt(min = 10, maxExclusive = 20)
        val campaigns: List<Campaign> = (0 until campaignCount).map { generateCampaign() }
        val campaignIds = campaigns.map { it.id }

        val adGroupsCount = randomInt(min = 100, maxExclusive = 200)
        val adGroups: List<AdGroup> = (0 until adGroupsCount).map { generateAdGroup(campaignIds.random()) }
        val adGroupIds = adGroups.map { it.id }

        val bannerCount = randomInt(min = 100, maxExclusive = 200)
        val banners: List<Banner> = (0 until bannerCount).map { generateBanners(adGroupIds.random()) }

        return ClientsData(
            id = randomLong(),
            name = randomString(),

            campaigns = campaigns,
            adGroups = adGroups,
            banners = banners,
        )
    }

    private fun generateCampaign(): Campaign {
        return Campaign(
            id = randomLong(),
            strategy = Strategy(
                type = randomInt(maxExclusive = 5),
                data = """{"opt1": 122123.000, "opt2": 0.0}""",
            ),
            href = randomString(minChars = 20, maxChars = 50),
        )
    }

    private fun generateAdGroup(campaignId: Long): AdGroup {
        return AdGroup(
            id = randomLong(),
            campaignId = campaignId,

            regions = (0..10).map { randomLong() },
            keywords = (0..100).map { randomString() },
            multipliers = (0..5).map {
                Multiplier(
                    condition = """{"sample": "condition", "with_value": 50""",
                    multiplier = 2,
                )
            }
        )
    }

    private fun generateBanners(adGroupId: Long): Banner {
        return Banner(
            id = randomLong(),
            adGroupId = adGroupId,

            text1 = randomString(),
            text2 = if (randomBoolean()) randomString() else null,

            image = if (randomBoolean(20)) randomString() else null,
            links = (0..randomInt(3)).map {
                Link(
                    href = randomString(),
                    text = randomString(),
                )
            },

            prices = (0..randomInt(5)).map {
                randomDouble()
            },
        )
    }

    private fun randomBoolean(bound: Int = 50): Boolean =
        randomInt(maxExclusive = 100) >= bound

    private fun randomLong(): Long =
        ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE)

    private fun randomInt(min: Int = 0, maxExclusive: Int = 1000): Int =
        ThreadLocalRandom.current().nextInt(min, maxExclusive)

    private fun randomDouble(): Double =
        ThreadLocalRandom.current().nextDouble()

    private fun randomString(minChars: Int = 5, maxChars: Int = 20) =
        RandomStringUtils.randomAlphanumeric(minChars, maxChars)
}
