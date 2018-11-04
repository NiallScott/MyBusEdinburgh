/*
 * Copyright (C) 2012 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link DialogFragment} allows the user to select bus services from a list and store the
 * user's selection. This may be used to ask the user to filter bus services or to say which
 * services they are interested in.
 * 
 * @author Niall Scott
 */
public class ServicesChooserDialogFragment extends DialogFragment {
    
    /** The argument name for services. */
    private static final String ARG_SERVICES = "services";
    /** The argument name for the default selected services. */
    private static final String ARG_SELECTED_SERVICES = "selectedServices";
    /** The argument name for the dialog title. */
    private static final String ARG_TITLE = "dialogTitle";

    /** Used to store the checked state between configuration changes. */
    private static final String STATE_CHECK_BOXES = "checkBoxes";

    private String[] services;
    private boolean[] checkBoxes;
    
    /**
     * Create a new instance of this {@link DialogFragment}, providing a list of services to
     * select from, a list of services to select by default and a title for the {@link Dialog}.
     * 
     * @param services The list of services to show to the user.
     * @param selectedServices The services to select by default, {@code null} if none.
     * @param dialogTitle The title to use for the {@link Dialog}.
     * @return A new instance of this {@link DialogFragment}.
     */
    public static ServicesChooserDialogFragment newInstance(final String[] services,
                                                            final String[] selectedServices,
                                                            final String dialogTitle) {
        final ServicesChooserDialogFragment f = new ServicesChooserDialogFragment();
        final Bundle b = new Bundle();
        b.putStringArray(ARG_SERVICES, services);
        b.putStringArray(ARG_SELECTED_SERVICES, selectedServices);
        b.putString(ARG_TITLE, dialogTitle);
        f.setArguments(b);
        
        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Bundle args = getArguments();
        services = args.getStringArray(ARG_SERVICES);
        
        // Do sanity checks.
        if (services == null || services.length == 0) {
            throw new IllegalArgumentException("A list of services must be supplied.");
        }
        
        if (savedInstanceState != null) {
            // If there is a previous instance, get the args from the saved
            // instance state.
            checkBoxes = savedInstanceState.getBooleanArray(STATE_CHECK_BOXES);
        } else {
            final String[] selectedServices = args.getStringArray(ARG_SELECTED_SERVICES);
            checkBoxes = new boolean[services.length];
            
            if (selectedServices != null && selectedServices.length > 0) {
                int i;
                final int len = services.length;

                for (i = 0; i < len; i++) {
                    for (String s : selectedServices) {
                        if (services[i].equals(s)) {
                            checkBoxes[i] = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save the state.
        outState.putBooleanArray(STATE_CHECK_BOXES, checkBoxes);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // Build the Dialog.
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(ARG_TITLE));
        builder.setMultiChoiceItems(services, checkBoxes,
                (dialog, which, isChecked) -> checkBoxes[which] = isChecked);

        builder.setPositiveButton(R.string.close, null);

        return builder.create();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        
        // Tell the listener that there may be changes.
        dispatchServicesChosen();
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
     * Get a {@link String} array of the chosen services.
     * 
     * @return A {@link String} array of the chosen services.
     */
    public String[] getChosenServices() {
        int counter = 0;
        
        // Firstly, count the number of chosen services so we know how big to make the String array.
        for (boolean b : checkBoxes) {
            if (b) {
                counter++;
            }
        }
        
        // If there's no chosen services, return an empty array.
        if (counter == 0) {
            return new String[] { };
        }
        
        // Create the array of the determined size.
        final String[] items = new String[counter];
        int i = 0;
        final int len = checkBoxes.length;
        
        // Loop through the check boxes, if it is selected, add it to the output String array.
        for (int j = 0; j < len; j++) {
            if (checkBoxes[j]) {
                items[i] = services[j];
                i++;
            }
        }
        
        return items;
    }
    
    /**
     * Get a {@link String} representation of the chosen services.
     * 
     * @param chosenServices A {@link String} array of chosen services.
     * @return A {@link String} representation of the chosen services.
     */
    public static String getChosenServicesAsString(final String[] chosenServices) {
        // If there are no chosen services, return an empty String.
        if (chosenServices == null || chosenServices.length == 0) {
            return "";
        }
        
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        
        for (String s : chosenServices) {
            if (isFirst) {
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
     * Dispatch chosen services to the {@link Callbacks} listener either provided by
     * {@link #getTargetFragment()} or {@link #getActivity()}.
     */
    private void dispatchServicesChosen() {
        final Fragment targetFragment = getTargetFragment();
        final Activity activity = getActivity();
        final Callbacks callbacks;

        if (targetFragment != null && targetFragment instanceof Callbacks) {
            callbacks = (Callbacks) targetFragment;
        } else if (activity != null && activity instanceof Callbacks) {
            callbacks = (Callbacks) activity;
        } else {
            callbacks = null;
        }

        if (callbacks != null) {
            callbacks.onServicesChosen(getChosenServices());
        }
    }
    
    /**
     * Any {@link Activity Activities} which host this {@link DialogFragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks {
        
        /**
         * This is called when the user dismisses the service chooser dialog. This will get called
         * even when no services are chosen, and may not necessarily mean that the user has made a
         * new selection.
         * 
         * @param chosenServices A {@link String} array of chosen services.
         */
        void onServicesChosen(String[] chosenServices);
    }
}