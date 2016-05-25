/*
 * Copyright (c) 2016.
 * Modified by marcelo.benites@aptoide.com on 19/05/2016.
 */

package com.aptoide.amethyst.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aptoide.amethyst.R;
import com.aptoide.dataprovider.webservices.models.v7.GetAppMeta;

public class InstallWarningDialog extends DialogFragment {

	private static final String INSTALL_WARNING_DIALOG_APP_RANK =
			"INSTALL_WARNING_DIALOG_APP_RANK";
	private static final String INSTALL_WARNING_DIALOG_TRUSTED_APP_AVAILABLE =
			"INSTALL_WARNING_DIALOG_TRUSTED_APP_AVAILABLE";

	private InstallWarningDialogListener listener;
	private String rank;
	private Button trustedAppButton;
	private boolean trustedVersionAvailable;
	private Button proceedButton;

	public static InstallWarningDialog newInstance(String rank, boolean trustedVersionAvailable) {

		final InstallWarningDialog dialog = new InstallWarningDialog();

		final Bundle bundle = new Bundle();
		bundle.putString(INSTALL_WARNING_DIALOG_APP_RANK, rank);
		bundle.putBoolean(INSTALL_WARNING_DIALOG_TRUSTED_APP_AVAILABLE, trustedVersionAvailable);
		dialog.setArguments(bundle);

		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof InstallWarningDialogListener) {
			listener = (InstallWarningDialogListener) activity;
		} else {
			throw new IllegalStateException("Activity must implement " +
					InstallWarningDialogListener.class.getSimpleName() + "in order to show this " +
					"dialog");
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rank = getArguments().getString(INSTALL_WARNING_DIALOG_APP_RANK);
		trustedVersionAvailable = getArguments().getBoolean
				(INSTALL_WARNING_DIALOG_TRUSTED_APP_AVAILABLE);
	}

	@SuppressLint("InflateParams")
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		final LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View contentView = inflater.inflate(R.layout.dialog_install_warning, null);

		setRank(contentView);
		setTextBadges(contentView);
		setProceedButton(contentView);
		setTrustedAppButton(contentView);
		dialogBuilder.setView(contentView);

		final AlertDialog dialog = dialogBuilder.create();
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		return dialog;
	}

	private void setRank(View contentView) {
		TextView badge = (TextView) contentView.findViewById(R.id
				.dialog_install_warning_rank_text);
		switch (rank) {
			case GetAppMeta.File.Malware.WARNING:
				badge.setText(R.string.warning);
				badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_badge_warning, 0, 0,
						0);
				break;
			case GetAppMeta.File.Malware.UNKNOWN:
				badge.setText(R.string.unknown);
				badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_badge_unknown, 0, 0,
						0);
				break;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		proceedButton.setOnClickListener(null);
		trustedAppButton.setOnClickListener(null);
		proceedButton = null;
		trustedAppButton = null;
	}

	private void setTextBadges(View contentView) {
		final SpannableString text = new SpannableString(contentView.getContext()
				.getString(R.string.dialog_install_warning_credibility_text));

		final ImageSpan trustedBadge = new ImageSpan(contentView.getContext(), R.drawable
				.ic_badge_trusted_small);

		text.setSpan(trustedBadge, text.length() - 14, text.length() - 13, 0);
		((TextView) contentView.findViewById(R.id.dialog_install_warning_credibility_text))
				.setText(text);
	}

	private void setProceedButton(View contentView) {
		proceedButton = (Button)contentView.findViewById(R.id.dialog_install_warning_proceed_button);
		proceedButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (listener != null) {
							listener.installApp();
						}
						dismiss();
					}
				});
	}

	public void setTrustedAppButton(View contentView) {

		trustedAppButton = (Button) contentView.findViewById(R.id.dialog_install_warning_trusted_app_button);
		final String topString;
		final String bottonString;
		if (trustedVersionAvailable) {
			topString = contentView.getContext().getString(R.string.dialog_install_warning_get_trusted_version_button);
			bottonString = contentView.getContext().getString(R.string.dialog_install_warning_trusted_version_button);
		} else {
			topString = contentView.getContext().getString(R.string.dialog_install_warning_search_for_trusted_app_button);
			bottonString = contentView.getContext().getString(R.string.dialog_install_warning_trusted_app_button);
		}
		final int topStringLength = topString.length();
		final int bottonStringLength = bottonString.length();

		final Spannable span = new SpannableString(topString + "\n" +  bottonString);
		span.setSpan(new StyleSpan(Typeface.BOLD), topStringLength, (topStringLength+bottonStringLength+1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		trustedAppButton.setText(span);

		trustedAppButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					if (trustedVersionAvailable) {
						listener.getTrustedAppVersion();
					} else {
						listener.searchForTrustedApp();
					}
				}
				dismiss();
			}
		});
	}
}
