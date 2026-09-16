package com.campus.campusmateriel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Fournit l'horloge de l'application.
 *
 * <p>Tous les composants qui ont besoin de la date courante recoivent cette horloge
 * par leur constructeur et utilisent {@code LocalDate.now(clock)}. Aucun appel direct
 * a {@code LocalDate.now()} sans argument ne doit exister dans le code de production.</p>
 *
 * <p>C'est ce qui rend les regles sur les dates verifiables de facon reproductible
 * (FR-024) : dans les tests, une horloge fixe remplace celle-ci, par exemple
 * {@code Clock.fixed(Instant.parse("2030-03-10T12:00:00Z"), ZoneOffset.UTC)}, ce qui
 * correspond a la date de recette imposee par l'enonce.</p>
 *
 * <p>La zone utilisee est celle du systeme : « aujourd'hui » designe la date du
 * serveur local (hypothese H-02), pas celle du navigateur du client.</p>
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
