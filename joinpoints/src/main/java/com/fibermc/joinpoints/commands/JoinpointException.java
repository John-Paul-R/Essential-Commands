package com.fibermc.joinpoints.commands;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.joinpoints.types.JoinpointLimit;

import net.minecraft.text.Text;

abstract class JoinpointException extends RuntimeException {

    public abstract Text message(ECText ecText);

    private abstract static class Tp extends JoinpointException {
        private final String ownerName;

        Tp(String ownerName) {
            this.ownerName = ownerName;
        }

        public String getOwnerName() {
            return ownerName;
        }
    }

    private abstract static class WithName extends Tp {
        private final String joinpointName;

        WithName(String joinpointName, String ownerName) {
            super(ownerName);
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }
    }

    static final class TpNotFound extends WithName {
        TpNotFound(String joinpointName, String ownerName) {
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
        TpNoAccess(String joinpointName, String ownerName) {
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
        OwnerNotFound(String ownerName) {
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

    abstract static class Share extends JoinpointException {
        private final String joinpointName;

        Share(String joinpointName) {
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }
    }

    static final class NotFound extends Share {
        NotFound(String joinpointName) {
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
        AlreadyGlobal(String joinpointName) {
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
        NoNewPlayers(String joinpointName) {
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
        PlayersNotShared(String joinpointName) {
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
        CannotClearGlobal(String joinpointName) {
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

    abstract static class Set extends JoinpointException {
        private final String joinpointName;

        Set(String joinpointName) {
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }

        static final class DeleteNotFound extends Set {
            DeleteNotFound(String joinpointName) {
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
            DeleteGeneric(String joinpointName) {
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
            private final JoinpointLimit.JoinpointType limitType;

            MaxPointsExceeded(String joinpointName, int max, int current, JoinpointLimit.JoinpointType limitType) {
                super(joinpointName);
                this.max = max;
                this.current = current;
                this.limitType = limitType;
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
                    ecText.accent(this.getJoinpointName()),
                    ecText.accent(String.valueOf(this.getMax())),
                    ecText.accent(limitType.name().toLowerCase())
                );
            }
        }
    }
}

