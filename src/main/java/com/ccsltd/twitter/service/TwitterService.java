package com.ccsltd.twitter.service;

import com.ccsltd.twitter.entity.Follower;
import com.ccsltd.twitter.entity.Friend;
import com.ccsltd.twitter.entity.ProcessControl;
import com.ccsltd.twitter.repository.FollowerRepository;
import com.ccsltd.twitter.repository.FriendRepository;
import com.ccsltd.twitter.repository.ProcessControlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import twitter4j.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RequiredArgsConstructor
@Slf4j
@Service
public class TwitterService {

    public static final String FOLLOWERS_SER = "followers.ser";
    public static final String FRIENDS_SER = "friends.ser";

//    public static void main(String[] args) {
//        TwitterService twitterService = new TwitterService(null, null, null);
//        PagableResponseList<User> followers = twitterService.deserialiseList(FOLLOWERS_SER);
//        System.out.println(followers.size());
//    }

    private final FollowerRepository followerRepository;
    private final ProcessControlRepository processControlRepository;
    private final FriendRepository friendRepository;

    public String initialiseData(String status) {

        if ("prepare".equals(status)) {
            if (processControlRepository.existsById(1L)) {
                processControlRepository.deleteById(1L);
            }
            processControlRepository.save(new ProcessControl(1L, "initialise", "prepared"));

            return "initialise status is 'prepared' to execute";
        } else if ("execute".equals(status)) {
            ProcessControl currentProcess = processControlRepository.findStatusByProcess("initialise");

            if (currentProcess == null || !"prepared".equals(currentProcess.getStatus())) {
                return "initialise status is not 'prepared'";
            }

            List<Follower> followers = getFollowers();
            serialiseList(followers, FOLLOWERS_SER, false);

            for (Follower follower : followers) {
                try {
                    followerRepository.save(follower);
                } catch (Exception cve) {
                    System.out.println(format("Duplicate Twitter Id {%s}", follower.getTwitterId()));
                }
            }

            pauseSeconds(10);

            List<Friend> friends = getFriends();
            serialiseList(friends, FRIENDS_SER, false);
            friendRepository.saveAll(friends);

            processControlRepository.deleteById(1L);

            return format("'%s' Followers created, '%s' Followers created", followers.size(), friends.size());
        } else {
            return format("invalid initialise status '%s'", status);
        }
    }

    public String refreshData() {
        int newFolowerIds = updateFollowers();
        int newFriendIds = updateFriends();

        return format("'%s' Followers added, '%s' Followers added", newFolowerIds, newFriendIds);
    }

    private List<Follower> getFollowers() {
        Twitter twitter = new TwitterFactory().getInstance();
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        long sleepMilliSeconds = 60000l;
        List<Follower> allUsers = new ArrayList<>();
        int fakeCount = 0;
        boolean isDebug = "true".equals(System.getenv("DEBUG"));

        followerRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFollowersList(screenName, nextCursor, maxResults);

                for (User user : partialUsers) {
                    //@formatter:off
                    Follower follower = Follower.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    allUsers.add(follower);
                }

                System.out.println("Total Followers retrieved: " + allUsers.size());

                //todo debug
                if(isDebug) {
                    fakeCount++;
                    if (fakeCount == 2) {
                        break;
                    }
                }
            } catch (TwitterException te) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private int updateFollowers() {
        Twitter twitter = new TwitterFactory().getInstance();
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        long sleepMilliSeconds = 60000l;
        List<Long> newUsers = new ArrayList<>();
        int fakeCount = 0;

        do {
            try {
                partialUsers = twitter.getFollowersIDs(screenName, nextCursor, maxResults);

                for (Long id : partialUsers.getIDs()) {
                    Optional<Follower> user = followerRepository.findByTwitterId(id);

                    if (!user.isPresent()) {
                        newUsers.add(id);
                    }
                }
            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        if (newUsers.size() > 0) {
            try {
                long[] array = new long[newUsers.size()];
                int i = 0;
                for (Long newUser : newUsers) {
                    array[i] = newUsers.get(i);
                    i++;
                }

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);

                for (User user : usersToAdd) {
                    //@formatter:off
                    Follower follower = Follower.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    followerRepository.save(follower);
                }

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return newUsers.size();
    }

    private List<Friend> getFriends() {
        Twitter twitter = new TwitterFactory().getInstance();
        PagableResponseList<User> partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 200;
        long sleepMilliSeconds = 60000l;
        List<Friend> allUsers = new ArrayList<>();
        int fakeCount = 0;
        boolean isDebug = "true".equals(System.getenv("DEBUG"));

        friendRepository.deleteAll();

        do {
            try {
                partialUsers = twitter.getFriendsList(screenName, nextCursor, maxResults);

                for (User user : partialUsers) {
                    //@formatter:off
                    Friend friend = Friend.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    allUsers.add(friend);
                }

                System.out.println("Total Friends retrieved: " + allUsers.size());

                //todo debug
                if(isDebug) {
                    fakeCount++;
                    if (fakeCount == 2) {
                        break;
                    }
                }
            } catch (TwitterException te) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        return allUsers;
    }

    private int updateFriends() {
        Twitter twitter = new TwitterFactory().getInstance();
        IDs partialUsers = null;
        String screenName = "ade_bald";
        long nextCursor = -1;
        int maxResults = 5000;
        long sleepMilliSeconds = 60000l;
        List<Long> newUsers = new ArrayList<>();
        int fakeCount = 0;

        do {
            try {
                partialUsers = twitter.getFriendsIDs(screenName, nextCursor, maxResults);

                for (Long id : partialUsers.getIDs()) {
                    Optional<Friend> user = friendRepository.findByTwitterId(id);

                    if (!user.isPresent()) {
                        newUsers.add(id);
                    }
                }
            } catch (TwitterException e) {
                System.out.println("Rate limit reached, waiting :" + sleepMilliSeconds / 1000L + " seconds");

                pauseSeconds(60);
            }
        } while ((nextCursor = partialUsers.getNextCursor()) != 0);

        if (newUsers.size() > 0) {
            try {
                long[] array = new long[newUsers.size()];
                int i = 0;
                for (Long newUser : newUsers) {
                    array[i] = newUsers.get(i);
                    i++;
                }

                ResponseList<User> usersToAdd = twitter.lookupUsers(array);

                for (User user : usersToAdd) {
                    //@formatter:off
                    Friend friend = Friend.builder()
                            .twitterId(user.getId())
                            .screenName(user.getScreenName())
                            .name(user.getName())
                            .description(user.getDescription())
                            .location(user.getLocation())
                            .followersCount(user.getFollowersCount())
                            .friendsCount(user.getFriendsCount())
                            .build();
                    //@formatter:on

                    friendRepository.save(friend);
                }

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }

        return newUsers.size();
    }

    private void serialiseList(List<?> list, String fileName, boolean append) {
        ObjectOutputStream oos = null;
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(fileName, append);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(list);
            oos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private PagableResponseList<User> deserialiseList(String fileName) {
        //        String s = "Hello World";
        //        byte[] b = {'e', 'x', 'a', 'm', 'p', 'l', 'e'};
        //
        //        try {
        //            // create a new file with an ObjectOutputStream
        //            FileOutputStream out = new FileOutputStream("/home/ade/Documents/Projects/java/Twitter/test.txt");
        //            ObjectOutputStream oout = new ObjectOutputStream(out);
        //
        //            // write something in the file
        //            oout.writeObject(s);
        //            oout.writeObject(b);
        //            oout.flush();
        //
        //            // create an ObjectInputStream for the file we created before
        //            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("test.txt"));
        //
        //            // read and print an object and cast it as string
        //            System.out.println("" + (String) ois.readObject());
        //
        //            // read and print an object and cast it as string
        //            byte[] read = (byte[]) ois.readObject();
        //            String s2 = new String(read);
        //            System.out.println("" + s2);
        //        } catch (Exception ex) {
        //            ex.printStackTrace();
        //        }
        PagableResponseList<User> list = null;
        //        List<User> list = null;
        try {
            FileInputStream fileIn = new FileInputStream("/home/ade/Documents/Projects/java/Twitter/" + fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            list = (PagableResponseList<User>) in.readObject();
            in.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void pauseSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
}

