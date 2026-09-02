import { describe, expect, it } from 'vitest'
import { calculateReadiness, competitionChecks } from './competitionReadiness'

describe('competition readiness', () => {
  it('does not report readiness while any critical declaration is missing', () => {
    const result = calculateReadiness({ age: true })
    expect(result.ready).toBe(false)
    expect(result.blockers.length).toBe(competitionChecks.length - 1)
  })

  it('reports readiness only after every declaration is confirmed', () => {
    const checks = Object.fromEntries(competitionChecks.map(item => [item.id, true]))
    const result = calculateReadiness(checks)
    expect(result.ready).toBe(true)
    expect(result.percent).toBe(100)
  })
})

