import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { api } from '../../lib/api'
import { degreeErrorMessage } from '../../lib/degree'
import { Modal } from '../ui/Modal'
import { Button } from '../ui/Button'
import { Input } from '../ui/Field'

export function ConfigProposalModal({ open, onClose, config }) {
  const [form, setForm] = useState({
    quorumRequired: config?.quorumRequired || 10,
    approvalThreshold: config?.approvalThreshold ?? 0.6,
    maxPendingUploads: config?.maxPendingUploads || 3,
    masterVoteWeight: config?.masterVoteWeight ?? 2,
    userVoteWeight: config?.userVoteWeight ?? 1,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: () =>
      api.config.propose({
        quorumRequired: Number(form.quorumRequired),
        approvalThreshold: Number(form.approvalThreshold),
        maxPendingUploads: Number(form.maxPendingUploads),
        masterVoteWeight: Number(form.masterVoteWeight),
        userVoteWeight: Number(form.userVoteWeight),
      }),
    onSuccess: () => {
      toast.success('Propuesta de política creada', {
        description: 'La votan todos los masters de la plataforma.',
      })
      onClose()
    },
    onError: (requestError) => setError(degreeErrorMessage(requestError)),
  })

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Proponer un cambio de política"
      description="No se aplica de inmediato. Se abre una votación global entre los masters de todos los grados."
    >
      <form
        onSubmit={(event) => {
          event.preventDefault()
          setError('')
          mutation.mutate()
        }}
        className="space-y-4"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Quórum mínimo"
            type="number"
            min={1}
            max={10000}
            value={form.quorumRequired}
            onChange={(event) => setForm((prev) => ({ ...prev, quorumRequired: event.target.value }))}
          />
          <Input
            label="Umbral de aprobación (0–1)"
            type="number"
            min={0.01}
            max={1}
            step={0.01}
            value={form.approvalThreshold}
            onChange={(event) => setForm((prev) => ({ ...prev, approvalThreshold: event.target.value }))}
          />
          <Input
            label="Máx. subidas pendientes"
            type="number"
            min={1}
            value={form.maxPendingUploads}
            onChange={(event) => setForm((prev) => ({ ...prev, maxPendingUploads: event.target.value }))}
          />
          <Input
            label="Peso del voto master"
            type="number"
            min={0.1}
            step={0.1}
            value={form.masterVoteWeight}
            onChange={(event) => setForm((prev) => ({ ...prev, masterVoteWeight: event.target.value }))}
          />
          <Input
            label="Peso del voto usuario"
            type="number"
            min={0.1}
            step={0.1}
            value={form.userVoteWeight}
            onChange={(event) => setForm((prev) => ({ ...prev, userVoteWeight: event.target.value }))}
          />
        </div>
        {error && (
          <div
            role="alert"
            className="rounded-2xl border border-[#e7c7c2] bg-[#fbefed] px-4 py-3 text-xs text-[#86483f]"
          >
            {error}
          </div>
        )}
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" loading={mutation.isPending}>
            Crear propuesta
          </Button>
        </div>
      </form>
    </Modal>
  )
}
