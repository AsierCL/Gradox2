-- V2: admin governance - audit trail actions BAN/UNBAN + config change proposals.
-- Postgres auto-names inline column checks as {table}_{column}_check.

-- audit_records.action_type: add BAN/UNBAN
alter table if exists audit_records drop constraint if exists audit_records_action_type_check;
alter table if exists audit_records add constraint audit_records_action_type_check
    check (action_type in ('UPLOAD','DELETE','VETO','POLICY_CHANGE','PROMOTION','EXPULSION','BAN','UNBAN'));

-- proposals.action_type: keep in sync with the enum
alter table if exists proposals drop constraint if exists proposals_action_type_check;
alter table if exists proposals add constraint proposals_action_type_check
    check (action_type in ('UPLOAD','DELETE','VETO','POLICY_CHANGE','PROMOTION','EXPULSION','BAN','UNBAN'));

-- config_proposal: JOINED inheritance child of proposals (proposal_type = 3)
create table if not exists config_proposal (
    id bigint not null,
    proposed_quorum_required integer,
    proposed_approval_threshold float(53),
    proposed_max_pending_uploads integer,
    proposed_master_vote_weight float(53),
    proposed_user_vote_weight float(53),
    primary key (id)
);

alter table if exists config_proposal
    add constraint FK_config_proposal_proposal
    foreign key (id) references proposals;
