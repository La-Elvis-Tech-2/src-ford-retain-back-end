package com.fordretain.api.dealer;

import java.math.BigDecimal;

public record DealerResponse(Long id, String name, String address, String district, BigDecimal rating,
		int reviewCount) {

	public static DealerResponse from(Dealer dealer) {
		return new DealerResponse(dealer.getId(), dealer.getName(), dealer.getAddress(), dealer.getDistrict(),
				dealer.getRating(), dealer.getReviewCount());
	}
}
