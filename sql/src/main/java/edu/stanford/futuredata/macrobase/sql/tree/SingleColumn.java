/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.stanford.futuredata.macrobase.sql.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public class SingleColumn extends SelectItem {

    private final Optional<Identifier> alias;
    private final Expression expression;

    public SingleColumn(Expression expression) {
        this(Optional.empty(), expression, Optional.empty());
    }

    public SingleColumn(Expression expression, Optional<Identifier> alias) {
        this(Optional.empty(), expression, alias);
    }

    public SingleColumn(Expression expression, Identifier alias) {
        this(Optional.empty(), expression, Optional.of(alias));
    }

    public SingleColumn(NodeLocation location, Expression expression, Optional<Identifier> alias) {
        this(Optional.of(location), expression, alias);
    }

    private SingleColumn(Optional<NodeLocation> location, Expression expression,
        Optional<Identifier> alias) {
        super(location);
        requireNonNull(expression, "expression is null");
        requireNonNull(alias, "alias is null");

        this.expression = expression;
        this.alias = alias;
    }

    public Optional<Identifier> getAlias() {
        return alias;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SingleColumn other = (SingleColumn) obj;
        return Objects.equals(this.alias, other.alias) && Objects
            .equals(this.expression, other.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, expression);
    }

    @Override
    public String toString() {
        // column name for the UDF is either 1) the user-provided alias, or 2) the function name
        // and arguments concatenated by "_"
        return alias.map(Identifier::toString).orElseGet(() -> formatForCol(expression));
    }

    /**
     * @return If the Expression is a Function Call (e.g., a UDF), concatenate the function name
     * and the arguments with "_". Otherwise, return the output of toString()
     */
    private String formatForCol(final Expression expr) {
        if (expr instanceof FunctionCall) {
            final FunctionCall func = (FunctionCall) expr;
            // for now, if UDF is a.b.c.d(), ignore "a.b.c."
            final String funcName = func.getName().getSuffix();
            return funcName + "_" + Joiner.on("_")
                .join(func.getArguments().stream().map(Expression::toString).iterator());
        }
        return expr.toString();

    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitSingleColumn(this, context);
    }

    @Override
    public List<Node> getChildren() {
        return ImmutableList.of(expression);
    }
}