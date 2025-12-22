package com.dswan.mtg.domain.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Prices {
    private String usd;
    private String usd_foil;
    private String usd_etched;

    @Override
    public String toString() {
        return "Normal: $" + usd + '\n' +
                "Foil: $" + usd_foil + '\n' +
                "Etched: $" + usd_etched;
    }
}
