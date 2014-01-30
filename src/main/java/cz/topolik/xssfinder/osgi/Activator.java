package cz.topolik.xssfinder.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Tomas Polesovsky
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Hi!");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Bye!");
    }
}
