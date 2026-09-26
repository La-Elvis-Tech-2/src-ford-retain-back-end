package com.fordretain.api.dto.response;

import java.math.BigDecimal;

import com.fordretain.api.model.Dealer;

public record DealerResponse(Long id, String name, String address, String district, BigDecimal rating,
		int reviewCount) {

	public static DealerResponse from(Dealer dealer) {
		return new DealerResponse(dealer.getId(), dealer.getName(), dealer.getAddress(), dealer.getDistrict(),
				dealer.getRating(), dealer.getReviewCount());
	}
}
