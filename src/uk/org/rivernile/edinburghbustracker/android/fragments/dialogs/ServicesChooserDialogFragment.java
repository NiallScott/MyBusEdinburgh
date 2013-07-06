/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 */

package uk.org.rivernile.edinburghbustracker.android.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import java.lang.ref.WeakReference;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This DialogFragment allows the user to select bus services from a list and
 * store the user's selection. This may be used to ask the user to filter bus
 * services or to say which services they are interested in.
 * 
 * @author Niall Scott
 */
public class ServicesChooserDialogFragment extends DialogFragment {
    
    /** The argument name for services. */
    private static final String ARG_SERVICES = "services";
    /** The argument name for the dialog title. */
    private static final String ARG_TITLE = "dialogTitle";
    /** The argument name for the default service. */
    public static final String ARG_DEFAULT_SERVICE = "defaultService";
    /** The argument name for check boxes, stored in the instance state. */
    private static final String ARG_CHECK_BOXES = "checkBoxes";
    
    private String[] services;
    private boolean[] checkBoxes;
    private String dialogTitle;
    private WeakReference<EventListener> listener;
    
    /**
     * Create a new instance of the ServicesChooserDialogFragment, without
     * providing a default service.
     * 
     * @param services The list of services to choose from.
     * @param dialogTitle The title to display in the Dialog.
     * @param listener Where events should be called back to.
     * @return A new instance of this DialogFragment.
     * @see #newInstance(java.lang.String[], java.lang.String,
     * java.lang.String,
     * uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
     * .ServicesChooserDialogFragment.EventListener) 
     */
    public static ServicesChooserDialogFragment newInstance(
            final String[] services, final String dialogTitle,
            final EventListener listener) {
        final ServicesChooserDialogFragment f =
                new ServicesChooserDialogFragment();
        final Bundle b = new Bundle();
        b.putStringArray(ARG_SERVICES, services);
        b.putString(ARG_TITLE, dialogTitle);
        f.setArguments(b);
        f.setListener(listener);
        f.setup();
        
        return f;
    }
    
    /**
     * Create a new instance of the ServicesChooserDialogFragment, providing a
     * default service.
     * 
     * @param services The list of services to choose from.
     * @param dialogTitle The title to display in the Dialog.
     * @param defaultService A service which is selected by default.
     * @param listener Where events should be called back to.
     * @return A new instance of this DialogFragment.
     * @see #newInstance(java.lang.String[], java.lang.String,
     * uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
     * .ServicesChooserDialogFragment.EventListener) 
     */
    public static ServicesChooserDialogFragment newInstance(
            final String[] services, final String dialogTitle,
            final String defaultService, final EventListener listener) {
        final ServicesChooserDialogFragment f =
                new ServicesChooserDialogFragment();
        final Bundle b = new Bundle();
        b.putStringArray(ARG_SERVICES, services);
        b.putString(ARG_TITLE, dialogTitle);
        b.putString(ARG_DEFAULT_SERVICE, defaultService);
        f.setArguments(b);
        f.setListener(listener);
        f.setup();
        
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState != null) {
            // If there is a previous instance, get the args from the saved
            // instance state.
            services = savedInstanceState.getStringArray(ARG_SERVICES);
            dialogTitle = savedInstanceState.getString(ARG_TITLE);
            checkBoxes = savedInstanceState.getBooleanArray(ARG_CHECK_BOXES);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save the state.
        outState.putString(ARG_TITLE, dialogTitle);
        outState.putStringArray(ARG_SERVICES, services);
        outState.putBooleanArray(ARG_CHECK_BOXES, checkBoxes);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // Build the Dialog.
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setTitle(dialogTitle);
        builder.setMultiChoiceItems(services, checkBoxes,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int which, boolean isChecked) {
                // Change the flag for that service.
                checkBoxes[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.close,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog,
                    final int id) {
                dismiss();
            }
        });

        return builder.create();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        
        // Tell the listener that there may be changes.
        if (listener != null) {
            final EventListener listenerRef = listener.get();
            if (listenerRef != null) {
                listenerRef.onServicesChosen();
            }
        }
    }
    
    /**
     * Initialise the data held within the Fragment before the Dialog is shown.
     */
    private void setup() {
        final Bundle b = getArguments();
        if(b == null)
            throw new IllegalStateException("Arguments were not supplied " +
                    "to ServicesChooserDialogFragment.");
        
        services = b.getStringArray(ARG_SERVICES);
        dialogTitle = b.getString(ARG_TITLE);

        // Do sanity checks.
        if(services == null || services.length == 0)
            throw new IllegalArgumentException("A list of services must " +
                    "be supplied.");

        if(dialogTitle == null)
            throw new IllegalArgumentException("The dialogTitle cannot " +
                    "be null.");

        // Create a parallell array which is the same size as the services
        // array.
        checkBoxes = new boolean[services.length];

        if(b.containsKey(ARG_DEFAULT_SERVICE)) {
            final String defaultService = b.getString(ARG_DEFAULT_SERVICE);

            final int len = services.length;
            for(int i = 0; i < len; i++) {
                if(services[i].equals(defaultService)) {
                    checkBoxes[i] = true;
                    break;
                }
            }
        }
    }
    
    /**
     * Set the listener which listens out for dialog events.
     * 
     * @param listener Where to call back to.
     */
    public void setListener(final EventListener listener) {
        if (listener == null) {
            this.listener = null;
        } else {
            this.listener = new WeakReference<EventListener>(listener);
        }
    }
    
    /**
     * Get the list of services that was supplied to the constructor.
     * 
     * @return A list of bus services to choose from.
     */
    public String[] getServices() {
        return services;
    }
    
    /**
     * Get a String array of the chosen services.
     * 
     * @return A String array of the chosen services.
     */
    public String[] getChosenServices() {
        int counter = 0;
        
        // Firstly, count the number of chosen services so we know how big to
        // make the String array.
        for(boolean b : checkBoxes) {
            if(b) counter++;
        }
        
        // If there's no chosen services, return an empty array.
        if(counter == 0) return new String[] { };
        
        // Create the array of the determined size.
        final String[] items = new String[counter];
        int i = 0;
        final int len = checkBoxes.length;
        
        // Loop through the check boxes, if it is selected, add it to the output
        // String array.
        for(int j = 0; j < len; j++) {
            if(checkBoxes[j]) {
                items[i] = services[j];
                i++;
            }
        }
        
        return items;
    }
    
    /**
     * Get the chosen services as a String suitable for the SQL IN clause.
     * 
     * @return A String of chosen services suitable for the SQL IN clause.
     */
    public String getChosenServicesForSql() {
        return getChosenServicesForSql(getChosenServices());
    }
    
    /**
     * Get the chosen services as a String suitable for the SQL IN clause.
     * 
     * This static version exists for when it's not possible to hold an object
     * reference for this class but a list of services is known.
     * 
     * @param services A String array of services.
     * @return A String of chosen services suitable for the SQL IN clause.
     */
    public static String getChosenServicesForSql(final String[] chosen) {
        // If there are no chosen services, return an empty String.
        if(chosen == null || chosen.length == 0) return null;
        
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        
        for(String s : chosen) {
            if(isFirst) {
                // Used to format the String correctly.
                sb.append('\'').append(s).append('\'');
                isFirst = false;
            } else {
                sb.append(',').append('\'').append(s).append('\'');
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Get a String representation of the chosen services.
     * 
     * @return A String representation of the chosen services.
     */
    public String getChosenServicesAsString() {
        // No point in doing this work again.
        final String[] chosen = getChosenServices();
        // If there are no chosen services, return an empty String.
        if(chosen.length == 0) return "";
        
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        
        for(String s : chosen) {
            if(isFirst) {
                // Used to format the String correctly.
                sb.append(s);
                isFirst = false;
            } else {
                sb.append(',').append(' ').append(s);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * The EventListener is an interface that any class which wants callbacks
     * from this Fragment should implement.
     */
    public static interface EventListener {
        
        /**
         * This is called when the user dismisses the service chooser dialog.
         * This will get called even when no services are chosen, and may not
         * necessarily mean that the user has made a new selection.
         */
        public void onServicesChosen();
    }
}