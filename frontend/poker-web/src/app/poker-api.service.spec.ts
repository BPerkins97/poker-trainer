import { TestBed } from '@angular/core/testing';

import { PokerApiService } from './poker-api.service';

describe('PokerApiService', () => {
  let service: PokerApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PokerApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
