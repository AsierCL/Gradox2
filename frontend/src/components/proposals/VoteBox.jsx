import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Check, ThumbsDown, ThumbsUp, UsersFour, X } from '@phosphor-icons/react'
import { api } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { Button } from '../ui/Button'
import { Skeleton } from '../ui/Spinner'
import { cn } from '../../lib/cn'

export function VoteBox({ proposalId, status = 'PENDING', compact = false }) {
  const { isAuthenticated } = useAuth()
  const queryClient = useQueryClient()
  const pending = status === 'PENDING'

  const resultsQuery = useQuery({
    queryKey: ['vote-results', String(proposalId)],
    queryFn: () => api.votes.results(proposalId),
    enabled: Boolean(proposalId),
  })

  const myVoteQuery = useQuery({
    queryKey: ['my-vote', String(proposalId)],
    queryFn: () => api.votes.mine(proposalId),
    enabled: Boolean(proposalId) && isAuthenticated,
    retry: false,
  })

  const configQuery = useQuery({
    queryKey: ['vote-config'],
    queryFn: () => api.config.get(),
    staleTime: 2 * 60_000,
    enabled: isAuthenticated,
  })

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['vote-results', String(proposalId)] })
    queryClient.invalidateQueries({ queryKey: ['my-vote', String(proposalId)] })
    queryClient.invalidateQueries({ queryKey: ['proposals'] })
  }

  const voteMutation = useMutation({
    mutationFn: (upvote) => api.votes.vote(proposalId, upvote),
    onSuccess: (_, upvote) => {
      toast.success(upvote ? 'Voto a favor registrado' : 'Voto en contra registrado')
      invalidate()
    },
    onError: (error) => toast.error(error.message),
  })

  const retractMutation = useMutation({
    mutationFn: () => api.votes.retract(proposalId),
    onSuccess: () => {
      toast.success('Voto retirado')
      invalidate()
    },
    onError: (error) => toast.error(error.message),
  })

  const upvotes = Number(resultsQuery.data?.upvotes || 0)
  const downvotes = Number(resultsQuery.data?.downvotes || 0)
  const total = upvotes + downvotes
  const approval = total ? (upvotes / total) * 100 : 0
  const threshold = Math.round(Number(configQuery.data?.approvalThreshold ?? 0.6) * 100)
  const quorum = Number(configQuery.data?.quorumRequired || 10)
  const myVote = myVoteQuery.data?.inFavor
  const loading = resultsQuery.isLoading

  return (
    <div
      className={cn(
        'rounded-3xl border border-ink/8 bg-white/38',
        compact ? 'p-4' : 'p-5 sm:p-6',
      )}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="eyebrow text-ink-faint">Recuento</p>
          {loading ? (
            <Skeleton className="mt-2 h-10 w-24" />
          ) : (
            <p className="display-tight mt-1.5 flex items-baseline gap-1.5 text-4xl tabular-nums text-ink">
              {Math.round(approval)}%
              <span className="text-[0.65rem] font-normal text-ink-faint">a favor</span>
            </p>
          )}
        </div>
        <span className="chip shrink-0 py-1 text-[0.55rem]">
          {total} {total === 1 ? 'voto' : 'votos'}
        </span>
      </div>

      <div className="relative mt-4 h-2 overflow-hidden rounded-full bg-ink/8">
        <div
          className="h-full rounded-full bg-gradient-to-r from-aqua-300 via-aqua-500 to-aqua-700 transition-all duration-700"
          style={{ width: `${total ? Math.max(approval, 2) : 0}%` }}
        />
      </div>
      <div className="mt-1.5 flex items-center justify-between text-[0.6rem] text-ink-faint">
        <span>{upvotes} a favor</span>
        <span>{downvotes} en contra</span>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-3 border-t border-ink/8 pt-4">
        <div>
          <p className="flex items-center gap-1.5 text-[0.6rem] uppercase tracking-[0.1em] text-ink-faint">
            <UsersFour size={12} aria-hidden="true" /> Quórum
          </p>
          <p className="mt-1 text-xs font-semibold tabular-nums text-ink">
            {total} / {quorum || '—'}
          </p>
        </div>
        <div>
          <p className="text-[0.6rem] uppercase tracking-[0.1em] text-ink-faint">Umbral mínimo</p>
          <p className="mt-1 text-xs font-semibold tabular-nums text-ink">
            {threshold}% a favor
          </p>
        </div>
      </div>

      {pending ? (
        isAuthenticated ? (
          <div className="mt-4">
            <p className="text-[0.62rem] text-ink-faint">Tu voto</p>
            <div className="mt-2 flex gap-2">
              <button
                type="button"
                onClick={() => voteMutation.mutate(true)}
                disabled={voteMutation.isPending || retractMutation.isPending}
                className={cn(
                  'inline-flex flex-1 items-center justify-center gap-1.5 rounded-full border px-3 py-2 text-[0.68rem] font-semibold transition-all',
                  myVote === true
                    ? 'border-aqua-700 bg-aqua-700 text-white'
                    : 'border-ink/10 bg-white/55 text-ink-soft hover:border-aqua-300 hover:text-aqua-800',
                )}
              >
                <ThumbsUp size={13} weight={myVote === true ? 'fill' : 'regular'} aria-hidden="true" />
                A favor
              </button>
              <button
                type="button"
                onClick={() => voteMutation.mutate(false)}
                disabled={voteMutation.isPending || retractMutation.isPending}
                className={cn(
                  'inline-flex flex-1 items-center justify-center gap-1.5 rounded-full border px-3 py-2 text-[0.68rem] font-semibold transition-all',
                  myVote === false
                    ? 'border-ink bg-ink text-white'
                    : 'border-ink/10 bg-white/55 text-ink-soft hover:border-ink/25 hover:text-ink',
                )}
              >
                <ThumbsDown size={13} weight={myVote === false ? 'fill' : 'regular'} aria-hidden="true" />
                En contra
              </button>
            </div>
            {myVote !== undefined && myVote !== null && (
              <button
                type="button"
                onClick={() => retractMutation.mutate()}
                disabled={retractMutation.isPending}
                className="mt-2 inline-flex items-center gap-1 text-[0.6rem] font-semibold text-ink-faint hover:text-ink"
              >
                <X size={11} weight="bold" aria-hidden="true" /> Retirar mi voto
              </button>
            )}
          </div>
        ) : (
          <Button to="/acceso" variant="ghost" className="mt-4 w-full min-h-9 text-xs">
            Entra para votar
          </Button>
        )
      ) : (
        <div className="mt-4 flex items-center gap-2 rounded-2xl bg-white/45 px-3 py-2.5 text-[0.65rem] text-ink-soft">
          <Check size={13} className="text-aqua-700" aria-hidden="true" />
          Votación cerrada · resultado definitivo
        </div>
      )}
    </div>
  )
}
