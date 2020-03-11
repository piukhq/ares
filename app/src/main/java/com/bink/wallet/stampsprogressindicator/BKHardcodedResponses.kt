package com.bink.wallet.stampsprogressindicator

import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType

class BKHardcodedResponses {
    companion object {
        val moshi: Moshi = Moshi.Builder().build()
        val typeC: ParameterizedType =
            Types.newParameterizedType(List::class.java, MembershipCard::class.java)
        val typeP: ParameterizedType =
            Types.newParameterizedType(List::class.java, MembershipPlan::class.java)
        val membershipCardsAdapter: JsonAdapter<List<MembershipCard>> = moshi.adapter(typeC)
        val membershipPlansAdapter: JsonAdapter<List<MembershipPlan>> = moshi.adapter(typeP)
        val BKMembershipCardsResponse = membershipCardsAdapter.fromJson(
            "[\n" +
                    "  {\n" +
                    "    \"id\": 29706,\n" +
                    "    \"membership_plan\": 246,\n" +
                    "    \"payment_cards\": [\n" +
                    "      {\n" +
                    "        \"id\": 5804,\n" +
                    "        \"active_link\": true\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": 5036,\n" +
                    "        \"active_link\": false\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": 5771,\n" +
                    "        \"active_link\": false\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": 5764,\n" +
                    "        \"active_link\": false\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"membership_transactions\": [],\n" +
                    "    \"status\": {\n" +
                    "      \"state\": \"authorised\",\n" +
                    "      \"reason_codes\": [\n" +
                    "        \"X300\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"card\": {\n" +
                    "      \"membership_id\": \"FF00000037892445\",\n" +
                    "      \"colour\": \"#000000\"\n" +
                    "    },\n" +
                    "    \"images\": [\n" +
                    "      {\n" +
                    "        \"id\": 1284,\n" +
                    "        \"type\": 0,\n" +
                    "        \"url\": \"https://api.staging.gb.bink.com/content/staging-media/hermes/schemes/ff-hero.png\",\n" +
                    "        \"description\": \"FatFace. Banner\",\n" +
                    "        \"encoding\": \"png\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": 1283,\n" +
                    "        \"type\": 3,\n" +
                    "        \"url\": \"https://api.staging.gb.bink.com/content/staging-media/hermes/schemes/ff-icon.png\",\n" +
                    "        \"description\": \"FatFace\",\n" +
                    "        \"encoding\": \"png\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"account\": {\n" +
                    "      \"tier\": 0\n" +
                    "    },\n" +
                    "    \"balances\": [\n" +
                    "      {\n" +
                    "        \"value\": 75.5,\n" +
                    "        \"currency\": \"GBP\",\n" +
                    "        \"prefix\": \"£\",\n" +
                    "        \"updated_at\": 1582642652\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"vouchers\": [\n" +
                    "      {\n" +
                    "        \"state\": \"inprogress\",\n" +
                    "        \"subtext\": \"for collecting\",\n" +
                    "        \"earn\": {\n" +
                    "          \"suffix\": \"stamps\",\n" +
                    "          \"type\": \"stamp\",\n" +
                    "          \"target_value\": 5,\n" +
                    "          \"value\": 3\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"Free\",\n" +
                    "          \"suffix\": \"Whopper\",\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"headline\": \"3 stamps to go!\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"issued\",\n" +
                    "        \"subtext\": \"for collecting\",\n" +
                    "        \"earn\": {\n" +
                    "          \"suffix\": \"stamps\",\n" +
                    "          \"type\": \"stamp\",\n" +
                    "          \"target_value\": 5,\n" +
                    "          \"value\": 5\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"Free\",\n" +
                    "          \"suffix\": \"Whopper\",\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"code\": \"BBB2222BBB\",\n" +
                    "        \"headline\": \"Earned\",\n" +
                    "        \"date_issued\": 1581503604,\n" +
                    "        \"expiry_date\": 1584144000\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"expired\",\n" +
                    "        \"subtext\": \"for collecting\",\n" +
                    "        \"earn\": {\n" +
                    "          \"suffix\": \"stamps\",\n" +
                    "          \"type\": \"stamp\",\n" +
                    "          \"target_value\": 5,\n" +
                    "          \"value\": 5\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"Free\",\n" +
                    "          \"suffix\": \"Whopper\",\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"code\": \"BBB4444BBB\",\n" +
                    "        \"headline\": \"Expired\",\n" +
                    "        \"date_issued\": 1578700800,\n" +
                    "        \"expiry_date\": 1581379200\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"redeemed\",\n" +
                    "        \"subtext\": \"for collecting\",\n" +
                    "        \"earn\": {\n" +
                    "          \"suffix\": \"stamps\",\n" +
                    "          \"type\": \"stamp\",\n" +
                    "          \"target_value\": 5,\n" +
                    "          \"value\": 5\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"Free\",\n" +
                    "          \"suffix\": \"Whopper\",\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"code\": \"BBB3333BBB\",\n" +
                    "        \"headline\": \"Redeemed\",\n" +
                    "        \"date_issued\": 1577836800,\n" +
                    "        \"expiry_date\": 1580428800,\n" +
                    "        \"date_redeemed\": 1579046400\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"inprogress\",\n" +
                    "        \"subtext\": \"for spending\",\n" +
                    "        \"earn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"type\": \"accumulator\",\n" +
                    "          \"target_value\": 100,\n" +
                    "          \"value\": 75\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"value\": 5,\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"headline\": \"£25.00 left to go!\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"issued\",\n" +
                    "        \"subtext\": \"for spending\",\n" +
                    "        \"earn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"type\": \"accumulator\",\n" +
                    "          \"target_value\": 100,\n" +
                    "          \"value\": 100\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"value\": 5,\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"code\": \"FFF2222FFF\",\n" +
                    "        \"headline\": \"Earned\",\n" +
                    "        \"date_issued\": 1581503604,\n" +
                    "        \"expiry_date\": 1584144000\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"expired\",\n" +
                    "        \"subtext\": \"for spending\",\n" +
                    "        \"earn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"type\": \"accumulator\",\n" +
                    "          \"target_value\": 100,\n" +
                    "          \"value\": 100\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"value\": 5,\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"code\": \"FFF4444FFF\",\n" +
                    "        \"headline\": \"Expired\",\n" +
                    "        \"date_issued\": 1578700800,\n" +
                    "        \"expiry_date\": 1581379200\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"state\": \"redeemed\",\n" +
                    "        \"subtext\": \"for spending\",\n" +
                    "        \"earn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"type\": \"accumulator\",\n" +
                    "          \"target_value\": 100,\n" +
                    "          \"value\": 100\n" +
                    "        },\n" +
                    "        \"burn\": {\n" +
                    "          \"prefix\": \"£\",\n" +
                    "          \"currency\": \"GBP\",\n" +
                    "          \"value\": 5,\n" +
                    "          \"type\": \"voucher\"\n" +
                    "        },\n" +
                    "        \"code\": \"FFF3333FFF\",\n" +
                    "        \"headline\": \"Redeemed\",\n" +
                    "        \"date_issued\": 1577836800,\n" +
                    "        \"expiry_date\": 1580428800,\n" +
                    "        \"date_redeemed\": 1579046400\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]"
        )

        val BKMembershipPlanResponse = membershipPlansAdapter.fromJson(
            "[\n" +
                    "  {\n" +
                    "    \"id\": 246,\n" +
                    "    \"status\": \"active\",\n" +
                    "    \"feature_set\": {\n" +
                    "      \"authorisation_required\": true,\n" +
                    "      \"transactions_available\": true,\n" +
                    "      \"digital_only\": false,\n" +
                    "      \"has_points\": true,\n" +
                    "      \"card_type\": 2,\n" +
                    "      \"linking_support\": [\n" +
                    "        \"ENROL\"\n" +
                    "      ],\n" +
                    "      \"apps\": [\n" +
                    "        {\n" +
                    "          \"app_type\": 0\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"app_type\": 1\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"card\": {\n" +
                    "      \"colour\": \"#000000\",\n" +
                    "      \"scan_message\": \"Scanning not required\"\n" +
                    "    },\n" +
                    "    \"images\": [\n" +
                    "      {\n" +
                    "        \"id\": 1284,\n" +
                    "        \"type\": 0,\n" +
                    "        \"url\": \"https://api.staging.gb.bink.com/content/staging-media/hermes/schemes/ff-hero.png\",\n" +
                    "        \"description\": \"FatFace. Banner\",\n" +
                    "        \"encoding\": \"png\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": 1283,\n" +
                    "        \"type\": 3,\n" +
                    "        \"url\": \"https://api.staging.gb.bink.com/content/staging-media/hermes/schemes/ff-icon.png\",\n" +
                    "        \"description\": \"FatFace\",\n" +
                    "        \"encoding\": \"png\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": 1285,\n" +
                    "        \"type\": 2,\n" +
                    "        \"url\": \"https://api.staging.gb.bink.com/content/staging-media/hermes/schemes/Offer_tile_6_BPN2Nnj.png\",\n" +
                    "        \"description\": \"Offer Tile Test\",\n" +
                    "        \"encoding\": \"png\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"account\": {\n" +
                    "      \"plan_name\": \"FatFace Rewards\",\n" +
                    "      \"plan_name_card\": \"FatFace Rewards\",\n" +
                    "      \"plan_url\": \"https://www.fatface.com\",\n" +
                    "      \"plan_summary\": \"FatFace Rewards\",\n" +
                    "      \"plan_description\": \"FatFace Rewards\",\n" +
                    "      \"plan_documents\": [\n" +
                    "        {\n" +
                    "          \"name\": \"Terms & Conditions\",\n" +
                    "          \"url\": \"https://bink.com/terms-and-conditions/\",\n" +
                    "          \"display\": [\n" +
                    "            \"VOUCHER\"\n" +
                    "          ],\n" +
                    "          \"checkbox\": false\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"barcode_redeem_instructions\": \"Test redeem instructions.\",\n" +
                    "      \"company_name\": \"FatFace\",\n" +
                    "      \"category\": \"Uncategorised\",\n" +
                    "      \"tiers\": [],\n" +
                    "      \"add_fields\": [],\n" +
                    "      \"authorise_fields\": [],\n" +
                    "      \"registration_fields\": []\n" +
                    "    },\n" +
                    "    \"balances\": [\n" +
                    "      {\n" +
                    "        \"currency\": \"GBP\",\n" +
                    "        \"prefix\": \"£\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"has_vouchers\": true,\n" +
                    "    \"content\": [\n" +
                    "      {\n" +
                    "        \"column\": \"Voucher_Stamps_Inprogress_Detail\",\n" +
                    "        \"value\": \"Text from merchant - how to earn voucher. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"column\": \"Voucher_Stamps_Issued_Detail\",\n" +
                    "        \"value\": \"Text from merchant - how to use voucher. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"column\": \"Voucher_Stamps_Redeemed_Detail\",\n" +
                    "        \"value\": \"Text from merchant - voucher has been redeemed. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"column\": \"Voucher_Stamps_Expired_Detail\",\n" +
                    "        \"value\": \"Text from merchant - vouhcer has expired. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]"
        )
    }
}