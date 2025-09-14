package com.fibermc.essentialcommands.commands.joinpoints;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import net.minecraft.text.Text;

abstract class JoinpointException extends RuntimeException {

    public abstract Text message(ECText ecText);

    private static abstract class Tp extends JoinpointException {
        private final String ownerName;

        public Tp(String ownerName) {
            this.ownerName = ownerName;
        }

        public String getOwnerName() {
            return ownerName;
        }
    }

    private static abstract class WithName extends Tp {
        private final String joinpointName;

        public WithName(String joinpointName, String ownerName) {
            super(ownerName);
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }
    }

    static final class TpNotFound extends WithName {
        public TpNotFound(String joinpointName, String ownerName) {
            super(joinpointName, ownerName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.tp.error.not_found",
                TextFormatType.Error,
                Text.literal(this.getJoinpointName()),
                Text.literal(this.getOwnerName())
            );
        }
    }

    static final class TpNoAccess extends WithName {
        public TpNoAccess(String joinpointName, String ownerName) {
            super(joinpointName, ownerName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.tp.error.no_access",
                TextFormatType.Error,
                Text.literal(this.getJoinpointName()),
                Text.literal(this.getOwnerName())
            );
        }
    }

    static final class OwnerNotFound extends Tp {
        public OwnerNotFound(String ownerName) {
            super(ownerName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.tp.error.owner_not_found",
                TextFormatType.Error,
                Text.literal(this.getOwnerName())
            );
        }
    }

    static abstract class Share extends JoinpointException {
        private final String joinpointName;

        public Share(String joinpointName) {
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }
    }

    static final class NotFound extends Share {
        public NotFound(String joinpointName) {
            super(joinpointName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.error.not_found",
                TextFormatType.Error,
                Text.literal(this.getJoinpointName())
            );
        }
    }

    static final class AlreadyGlobal extends Share {
        public AlreadyGlobal(String joinpointName) {
            super(joinpointName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.share.error.already_global",
                TextFormatType.Error,
                Text.literal(this.getJoinpointName())
            );
        }
    }

    static final class NoNewPlayers extends Share {
        public NoNewPlayers(String joinpointName) {
            super(joinpointName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.share.error.no_new_players",
                TextFormatType.Error
            );
        }
    }

    static final class PlayersNotShared extends Share {
        public PlayersNotShared(String joinpointName) {
            super(joinpointName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.share.error.players_not_shared",
                TextFormatType.Error
            );
        }
    }

    static final class CannotClearGlobal extends Share {
        public CannotClearGlobal(String joinpointName) {
            super(joinpointName);
        }

        @Override
        public Text message(ECText ecText) {
            return ecText.getText(
                "cmd.joinpoint.share.error.cannot_clear_global",
                TextFormatType.Error,
                ecText.accent(this.getJoinpointName())
            );
        }
    }

    static abstract class Set extends JoinpointException {
        private final String joinpointName;

        public Set(String joinpointName) {
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }

        static final class DeleteNotFound extends Set {
            public DeleteNotFound(String joinpointName) {
                super(joinpointName);
            }

            @Override
            public Text message(ECText ecText) {
                return ecText.getText(
                    "cmd.joinpoint.error.not_found",
                    TextFormatType.Error,
                    ecText.accent(this.getJoinpointName())
                );
            }
        }

        static final class DeleteGeneric extends Set {
            public DeleteGeneric(String joinpointName) {
                super(joinpointName);
            }

            @Override
            public Text message(ECText ecText) {
                return ecText.getText(
                    "cmd.joinpoint.delete.error",
                    TextFormatType.Error,
                    ecText.accent(this.getJoinpointName())
                );
            }
        }

        static final class MaxPointsExceeded extends Set {
            private final int max;
            private final int current;

            public MaxPointsExceeded(String joinpointName, int max, int current) {
                super(joinpointName);
                this.max = max;
                this.current = current;
            }

            public int getMax() {
                return max;
            }

            public int getCurrent() {
                return current;
            }

            @Override
            public Text message(ECText ecText) {
                return ecText.getText(
                    "cmd.joinpoint.set.error.limit",
                    TextFormatType.Error,
                    ecText.accent(this.getJoinpointName())
                );
            }
        }
    }
}

