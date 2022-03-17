package com.ccsltd.twitter.config;

import com.ccsltd.twitter.entity.Fixed;
import com.ccsltd.twitter.repository.FixedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DataLoader implements ApplicationRunner {

    private final FixedRepository fixedRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (fixedRepository.findAll().size() == 0) {
            Long id = 0L;

            //TODO use opencsv from file?
            insertFixed(1268497715496960001L, "Lee Rigby Foundation", "FoundationRigby");
            insertFixed(75343905L, "Calvin", "calvinrobinson");
            insertFixed(1404412545805471746L, "GBNews Fans", "GBNfans");
            insertFixed(924541717944131584L, "Neil Oliver", "thecoastguy");
            insertFixed(1339166129110065152L, "GB News", "GBNEWS");
            insertFixed(1308666120585572352L, "The Reclaim Party", "thereclaimparty");
            insertFixed(1269649283952893955L, "Defund The BBC", "DefundBBC");
            insertFixed(967900364L, "David Frost", "DavidGHFrost");
            insertFixed(1123143752984477696L, "Alex Phillips", "ThatAlexWoman");
            insertFixed(881883537191964672L, "Tim Martin", "TimMartinWS");
            insertFixed(81371986L, "Laurence Fox ⚪️", "LozzaFox");
            insertFixed(2231199386L, "Carole Malone", "thecarolemalone");
            insertFixed(2286483986L, "Lord Digby Jones", "Digbylj");
            insertFixed(15710120L, "Michelle Dewberry", "MichelleDewbs");
            insertFixed(1284146730L, "Kate Hoey", "CatharineHoey");
            insertFixed(278656915L, "Claire Fox", "Fox_Claire");
            insertFixed(61660254L, "Priti Patel", "pritipatel");
            insertFixed(136004952L, "Andrew Neil", "afneil");
            insertFixed(4691437897L, "Darren Grimes 🇬🇧", "darrengrimes_");
            insertFixed(35760904L, "Suzanne Evans 🌸", "SuzanneEvans1");
            insertFixed(93880122L, "John Redwood", "johnredwood");
            insertFixed(1124345151642587136L, "Belinda de Lucy", "BelindadeLucy");
            insertFixed(453473029L, "Tom Harwood", "tomhfh");
            insertFixed(3899497877L, "Euro Guido", "EuroGuido");
            insertFixed(1466783923L, "Richard Tice", "TiceRichard");
            insertFixed(763047432569651200L, "Change Britain", "Change_Britain");
            insertFixed(465973L, "Guido Fawkes", "GuidoFawkes");
            insertFixed(437814330L, "Isabel Oakeshott", "IsabelOakeshott");
            insertFixed(722953111107567618L, "Fishingfor Leave", "fishingforleave");
            insertFixed(3320886334L, "Labour Leave", "labourleave");
            insertFixed(4764882552L, "Dominic Raab", "DominicRaab");
            insertFixed(1094881364887986177L, "Reform UK", "reformparty_uk");
            insertFixed(3390728889L, "Arron Banks", "Arron_banks");
            insertFixed(3362016513L, "Leave.EU", "LeaveEUOfficial");
            insertFixed(2797521996L, "David Davis", "DavidDavisMP");
            insertFixed(885838630928994304L, "Jacob Rees - Mogg", "Jacob_Rees_Mogg");
            insertFixed(19017675L, "Nigel Farage", "Nigel_Farage");
        }
    }

    private void insertFixed(long id, String name, String screenName) {
        try {
            fixedRepository.save(new Fixed(id, name, screenName));
        } catch (DataIntegrityViolationException dive) {
            // do nothing
        }
    }
}
