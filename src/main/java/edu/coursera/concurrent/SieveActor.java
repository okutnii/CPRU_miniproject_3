package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);

        finish(() -> {
            for(int i = 3; i <= limit; i+= 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        SieveActorActor temp = sieveActor;
        int result = 0;
        while(temp != null) {
            result += temp.numLocalPrimes;
            temp = temp.nextActor;
        }
        return result;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */

        static final int MAX_LOCAL_PRIMES = 1000;

        int[] localPrimes = new int[MAX_LOCAL_PRIMES];
        private SieveActorActor nextActor = null;
        private int numLocalPrimes = 0;

        public SieveActorActor(int candidate) {
            localPrimes[numLocalPrimes] = candidate;
            numLocalPrimes++;
        }

        @Override
        public void process(final Object msg) {

            final int candidate = (Integer)msg;

            if(candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(msg);
                }
            } else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if(locallyPrime) {
                    if(numLocalPrimes < MAX_LOCAL_PRIMES) {
                        localPrimes[numLocalPrimes] = candidate;
                        numLocalPrimes++;
                    }
                    else if(nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    }
                    else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        private boolean isLocallyPrime(int x) {
            for(int i = 0; i < numLocalPrimes; i++) {
                if(x % localPrimes[i] == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}