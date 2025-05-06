package com.example.simplecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView expressionTextView, resultTextView;
    private StringBuilder expression = new StringBuilder();
    private boolean justEvaluated = false;  // Menandai bahwa hasil baru saja ditampilkan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expressionTextView = findViewById(R.id.expressionTextView);
        resultTextView = findViewById(R.id.resultTextView);

        int[] buttonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9, R.id.buttonAdd, R.id.buttonSubtract,
                R.id.buttonMultiply2, R.id.buttonDivide, R.id.buttonPercent,
                R.id.buttonComma
        };

        for (int id : buttonIds) {
            Button button = findViewById(id);
            button.setOnClickListener(v -> {
                if (justEvaluated) {
                    expression.setLength(0); // reset jika baru selesai hitung
                    justEvaluated = false;
                }
                expression.append(((Button) v).getText().toString());
                expressionTextView.setText(expression.toString());
            });
        }

        // Tombol 'x' untuk delete
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> {
            if (expression.length() > 0) {
                expression.deleteCharAt(expression.length() - 1);
                expressionTextView.setText(expression.toString());
            }
        });

        // Tombol 'C' untuk clear semua
        findViewById(R.id.buttonC).setOnClickListener(v -> {
            expression.setLength(0);
            expressionTextView.setText("");
            resultTextView.setText("0");
        });

        // Tombol '=' untuk evaluasi hasil
        findViewById(R.id.buttonEquals).setOnClickListener(v -> {
            try {
                String expr = expression.toString()
                        .replace(",", ".")
                        .replace("x", "") // x = delete, seharusnya tidak muncul
                        .replace("÷", "/");

                double result = eval(expr);
                String formattedResult;

                if (result == (long) result) {
                    formattedResult = String.format("%d", (long) result); // tanpa desimal jika bulat
                } else {
                    formattedResult = String.valueOf(result);
                }

                resultTextView.setText(formattedResult);
                justEvaluated = true;
            } catch (Exception e) {
                resultTextView.setText("Error");
                justEvaluated = true;
            }
        });
    }

    // Evaluasi ekspresi matematika sederhana
    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}
