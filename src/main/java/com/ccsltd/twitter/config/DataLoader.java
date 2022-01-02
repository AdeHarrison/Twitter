package com.ccsltd.twitter.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.ccsltd.twitter.entity.Fixed;
import com.ccsltd.twitter.repository.FixedRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DataLoader implements ApplicationRunner {

    private final FixedRepository fixedRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (fixedRepository.findAll().size() == 0) {
            Long id = 0L;

            //TODO use opencsv from file?
            insertFixed(id++, 1268497715496960001L, "Lee Rigby Foundation", "FoundationRigby");
            insertFixed(id++, 75343905L, "Calvin", "calvinrobinson");
            insertFixed(id++, 1404412545805471746L, "GBNews Fans", "GBNfans");
            insertFixed(id++, 924541717944131584L, "Neil Oliver", "thecoastguy");
            insertFixed(id++, 1339166129110065152L, "GB News", "GBNEWS");
            insertFixed(id++, 1308666120585572352L, "The Reclaim Party", "thereclaimparty");
            insertFixed(id++, 1269649283952893955L, "Defund The BBC", "DefundBBC");
            insertFixed(id++, 967900364L, "David Frost", "DavidGHFrost");
            insertFixed(id++, 1123143752984477696L, "Alex Phillips", "ThatAlexWoman");
            insertFixed(id++, 881883537191964672L, "Tim Martin", "TimMartinWS");
            insertFixed(id++, 81371986L, "Laurence Fox ⚪️", "LozzaFox");
            insertFixed(id++, 2231199386L, "Carole Malone", "thecarolemalone");
            insertFixed(id++, 2286483986L, "Lord Digby Jones", "Digbylj");
            insertFixed(id++, 15710120L, "Michelle Dewberry", "MichelleDewbs");
            insertFixed(id++, 1284146730L, "Kate Hoey", "CatharineHoey");
            insertFixed(id++, 278656915L, "Claire Fox", "Fox_Claire");
            insertFixed(id++, 61660254L, "Priti Patel", "pritipatel");
            insertFixed(id++, 136004952L, "Andrew Neil", "afneil");
            insertFixed(id++, 4691437897L, "Darren Grimes 🇬🇧", "darrengrimes_");
            insertFixed(id++, 35760904L, "Suzanne Evans 🌸", "SuzanneEvans1");
            insertFixed(id++, 93880122L, "John Redwood", "johnredwood");
            insertFixed(id++, 1124345151642587136L, "Belinda de Lucy", "BelindadeLucy");
            insertFixed(id++, 453473029L, "Tom Harwood", "tomhfh");
            insertFixed(id++, 3899497877L, "Euro Guido", "EuroGuido");
            insertFixed(id++, 1466783923L, "Richard Tice", "TiceRichard");
            insertFixed(id++, 763047432569651200L, "Change Britain", "Change_Britain");
            insertFixed(id++, 465973L, "Guido Fawkes", "GuidoFawkes");
            insertFixed(id++, 437814330L, "Isabel Oakeshott", "IsabelOakeshott");
            insertFixed(id++, 722953111107567618L, "Fishingfor Leave", "fishingforleave");
            insertFixed(id++, 3320886334L, "Labour Leave", "labourleave");
            insertFixed(id++, 4764882552L, "Dominic Raab", "DominicRaab");
            insertFixed(id++, 1094881364887986177L, "Reform UK", "reformparty_uk");
            insertFixed(id++, 3390728889L, "Arron Banks", "Arron_banks");
            insertFixed(id++, 3362016513L, "Leave.EU", "LeaveEUOfficial");
            insertFixed(id++, 2797521996L, "David Davis", "DavidDavisMP");
            insertFixed(id++, 885838630928994304L, "Jacob Rees - Mogg", "Jacob_Rees_Mogg");
            insertFixed(id++, 19017675L, "Nigel Farage", "Nigel_Farage");
        }
    }

    private void insertFixed(long id, long twitterId, String name, String screenName) {
        try {
            fixedRepository.save(new Fixed(id, twitterId, name, screenName));
        } catch (DataIntegrityViolationException dive) {
            // do nothing
        }
    }
}
