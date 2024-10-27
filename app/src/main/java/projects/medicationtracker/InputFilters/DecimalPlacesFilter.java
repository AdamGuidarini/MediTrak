package projects.medicationtracker.InputFilters;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;

import java.util.Locale;

public class DecimalPlacesFilter extends DigitsKeyListener {

    public DecimalPlacesFilter() {
        super(Locale.getDefault(), false, true);
    }

    @Override
    public CharSequence filter(
            CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend
    ) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);
        final int digits = 3;
        String regex = "([.,])";

        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }

        int len = end - start;

        if (len == 0) {
            return source;
        }

        String destString = dest.toString();

        if (destString.contains(".") || destString.contains(",")) {
            String[] split = destString.split(regex);

            return split.length == 2 && split[1].length() >= digits ?
                    "" : new SpannableStringBuilder(source, start, end);
        }

        String sourceString = source.toString();

        if (sourceString.contains(".") || sourceString.contains(",")) {
            String[] split = sourceString.split(regex);

            return split.length == 2 && split[1].length() >= digits ?
                    "" : new SpannableStringBuilder(source, start, end);
        }

        return new SpannableStringBuilder(source, start, end);
    }
}
