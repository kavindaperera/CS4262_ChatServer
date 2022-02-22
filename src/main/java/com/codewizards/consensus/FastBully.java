package com.codewizards.consensus;

import com.codewizards.Constants;
import io.reactivex.Completable;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class FastBully {

    public static Logger logger = Logger.getLogger(FastBully.class.getName());

    private static FastBully INSTANCE;

    @Getter @Setter
    private static boolean electionReady = true;

    public static synchronized FastBully getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FastBully();
        }
        return INSTANCE;
    }

    private FastBully() {
    }

    public String startElection(){
        logger.info("You started the election");
        setElectionReady(false);

        //TODO

        Completable.timer(Constants.ELECTION_TIMEOUT, TimeUnit.MILLISECONDS).subscribeWith(new DisposableCompletableObserver() {
            @Override
            public void onStart() {
                logger.info("Election timeout started!");
            }

            @Override
            public void onError(@NonNull Throwable error) {
                error.printStackTrace();
            }

            @Override
            public void onComplete() {
                logger.info("Election timeout completed!");
            }
        });

        return null;
    }

}
