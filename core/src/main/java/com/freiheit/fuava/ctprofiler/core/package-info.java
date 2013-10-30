/**
 * Call Tree Profiler for measuring execution times of your codepaths.
 * 
 * <p>The Call Tree Profiler is a tool to measure execution times of your codepaths.
 * It is not intended to profile each and every call, but to allow explicit definition
 * of the calls to profile, such that the output is easily readable and suitable
 * even for use during production to be able to quickly identify bottlenecks that
 * were not found during testing.</p>
 * 
 * <h4>Initialization</h4>
 * <p>To be most usefull, you will usually need to define a toplevel point for your profiling
 * where you render the Call Tree to a logger, and clear the CallTreeProfiler.
 * For example, this could be in your request processing:</p>
 *
 * <pre>
 * CallTreeProfiler profiler = ....
 * 
 * beforeRequestHandling(String requestname) {
 *   profiler.begin(requestname, System.nanos());
 * }
 * 
 * afterRequestHandling(String requestname) {
 * 
 *   profiler.end(requestname, System.nanos());
 *   
 *   String output = profiler.renderThreadStateAsText(new StringBuilder()).toString();
 *   System.out.println(output);
 *   
 *   // Important: clear the profiler state
 *   profiler.clear()
 * }
 * </pre>
 * 
 * <h4>Further Measures</h4>
 * <p>To get an interesting Calltree, you will now need to add some more measuring points. 
 * You have a lot of options for this, here are some suggestions:</p>
 * 
 * <h5>Method interceptor for your DAOs</h5>
 * <p>It has proved very usefull to add a Method Interceptor to your dependency injection framework
 * which wraps all calls to your DAOs. For example something like this: </p>
 * <pre>
   public class DaoProfiler implements MethodInterceptor {
     private TimeKeeper _timeKeeper = ...
     
     public Object invoke(MethodInvocation invocation) throws Throwable {
        final String key = _timeKeeper.isEnabled() ? method.getDeclaringClass().getSimpleName() + "." + method.getName() : null;
        _timeKeeper.begin(key);
        try {
            return invocation.proceed();
        } finally {
            _timeKeeper.end(key);
        }
     }
   }
   </pre>
 *
 * <h5>Explicit Measuring Points</h5>
 * <p>If you suspect your code in certain parts to be suboptimal, you could add simple measurements:</p>
 * <pre>
 * TimeKeeper timeKeeper = ...
 * timeKeeper.begin("name");
 * try {
 *   // do whatever
 * } finally {
 *   timeKeeper.end("name");
 * }
 * </pre>
 * 
 * <h5>Profile entire objects</h5>
 * <p>If you create objects for which you would like to add profiling of the methods transparently 
 * (similar to the Method Interceptor above) you can use the proxy functionality of the TimeKeeper:</p>
 * <pre>
 * TimeKeeper timeKeeper = ...
 * MyInterface myInstance = ...
 * myInstance = timeKeeper.proxy(MyInterface.class, myInstance);
 * </pre>
 * 
 * <h4>Example output</h4>
 * <p>A real-life example of the output you can expect:</p>
 * <pre>
[      1]      2033,22ms |-GET: /linkon/
[      1]       511,21ms | |-ViewResolver.resolveViewName
[      1]       498,97ms | | |-ViewResolver.resolveViewName
[      1]      1457,69ms | |-FragmentView.render
[      1]      1416,49ms | | |-FragmentView.primaryView.render
[      4]        28,29ms | | | |-ViewResolver.resolveViewName
[      4]        74,71ms | | | |-FragmentView.getContentString.render
[      1]         7,34ms | | |-ViewResolver.resolveViewName
[      1]        29,19ms | | |-FragmentView.layoutView.render
 * </pre>
 * <p>The first column in the output contains the number of calls on each path.</p>
 * 
 * <h4>Advanced: Include statistics of computations in different Threads</h4>
 * <p>If your application delegates work 
 * to worker threads, and later collects the results, you can even include 
 * the Call Tree of your delegated work. For example, you could do something
 * along the following lines:</p>
 * <pre>
 *   CallTreeProfiler profiler = ...
 *   Statistics statistics;
 *   profiler.begin("delegating", System.nanoTime());
 *   try {
 *     Future&lt;Statistics> statisticsFuture = ExecutorService.submit(new Callable&lt;Statistics>() {
 *       Statistics call() {
 *         CallTreeProfiler profiler = ...
 *         try {
 *         // do your work
 *      
 *           return profiler.getStatistics();
 *         } finally {
 *           profiler.clear();
 *         }
 *       }
 *     });
 *   
 *     // do some other stuff
 *     
 *     statistics = statisticsFuture.get();
 *   } finally {
 *     profiler.end("delegating", System.nanoTime(), statistics);
 *   }
 * </pre>
 * <p>When the output is rendered, the statistics that were associated like above will 
 * be marked. An example output is:</p>
 * <pre>
----------- BEGIN SUBTASK ----------
    [pool-4-thread-13]
    [      1]      3604,24ms |-
    [      5]      3585,78ms | |-AmadeusWebserviceAAO.sendRequest
    [      5]        97,85ms | | |-AxisStubConstructor
    [      1]       930,80ms | | |-AirMultiAvailability
    [      1]       930,75ms | | | |-AMADEUS-Webservice
    [      4]      2556,54ms | | |-FareQuoteItinerary
    [      4]      2556,31ms | | | |-AMADEUS-Webservice
    [     21]         0,57ms | |-LocationResolver.resolveLocationForIataAirportCode
    [      1]         0,06ms | |-SessionPool.return

----------- END   SUBTASK ----------
 * </pre>
 */
package com.freiheit.fuava.ctprofiler.core;

